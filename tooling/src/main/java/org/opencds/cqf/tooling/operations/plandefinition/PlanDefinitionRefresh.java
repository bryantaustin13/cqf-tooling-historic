package org.opencds.cqf.tooling.operations.plandefinition;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.util.BundleUtil;
import org.cqframework.cql.elm.requirements.fhir.DataRequirementsProcessor;
import org.cqframework.fhir.utilities.exception.IGInitializationException;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.r5.model.Library;
import org.hl7.fhir.r5.model.PlanDefinition;
import org.opencds.cqf.tooling.exception.InvalidOperationArgs;
import org.opencds.cqf.tooling.igtools.IGLoggingService;
import org.opencds.cqf.tooling.npm.LibraryLoader;
import org.opencds.cqf.tooling.operations.ExecutableOperation;
import org.opencds.cqf.tooling.operations.OperationParam;
import org.opencds.cqf.tooling.operations.library.LibraryPackage;
import org.opencds.cqf.tooling.processor.CqlProcessor;
import org.opencds.cqf.tooling.utilities.*;
import org.opencds.cqf.tooling.utilities.converters.ResourceAndTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PlanDefinitionRefresh implements ExecutableOperation {

    private static final Logger logger = LoggerFactory.getLogger(PlanDefinitionRefresh.class);

    @OperationParam(alias = { "ptpd", "pathtoPlanDefinition" }, setter = "setPathToPlanDefinition", required = true,
            description = "Path to the FHIR Plan Definition resource to refresh (required).")
    private String pathToPlanDefinition;
    @OperationParam(alias = { "ptcql", "pathtocql" }, setter = "setPathToCql", required = true,
            description = "Path to the CQL content referenced or depended on by the FHIR Library resource to refresh (required).")
    private String pathToCql;
    @OperationParam(alias = { "e", "encoding" }, setter = "setEncoding", defaultValue = "json",
            description = "The file format to be used for representing the resulting FHIR Library { json, xml } (default json)")
    private String encoding;
    @OperationParam(alias = { "v", "version" }, setter = "setVersion", defaultValue = "r4",
            description = "FHIR version { stu3, r4, r5 } (default r4)")
    private String version;
    @OperationParam(alias = { "op", "outputpath" }, setter = "setOutputPath",
            description = "The directory path to which the generated FHIR resources should be written (default is to replace existing resources within the IG)")
    private String outputPath;

    private IGUtils.IGInfo igInfo;
    private final FhirContext fhirContext;

    private final CqlProcessor cqlProcessor;

    private final List<LibraryPackage> libraryPackages;

    private final List<PlanDefinitionPackage> planDefinitionPackages;

    public PlanDefinitionRefresh(FhirContext fhirContext, String pathToCql) {
        this.fhirContext = fhirContext;
        this.pathToCql = pathToCql;
        this.libraryPackages = new ArrayList<>();
        this.planDefinitionPackages = new ArrayList<>();
        LibraryLoader libraryLoader = new LibraryLoader(this.fhirContext.getVersion().getVersion().getFhirVersionString());
        UcumEssenceService ucumService;
        try {
            ucumService = new UcumEssenceService(UcumEssenceService.class.getResourceAsStream("/ucum-essence.xml"));
        } catch (UcumException e) {
            throw new IGInitializationException("Could not create UCUM validation service", e);
        }
        this.cqlProcessor = new CqlProcessor(null,
                Collections.singletonList(this.pathToCql), libraryLoader, new IGLoggingService(logger),
                ucumService, null, null);
    }

    public PlanDefinitionRefresh(IGUtils.IGInfo igInfo, CqlProcessor cqlProcessor, List<LibraryPackage> libraryPackages) {
        this.igInfo = igInfo;
        this.fhirContext = igInfo.getFhirContext();
        this.cqlProcessor = cqlProcessor;
        this.libraryPackages = libraryPackages;
        this.planDefinitionPackages = new ArrayList<>();
    }

    @Override
    public void execute() {
        IBaseResource planDefinitionToRefresh = IOUtils.readResource(pathToPlanDefinition, fhirContext);
        if (!planDefinitionToRefresh.fhirType().equalsIgnoreCase("plandefinition")) {
            throw new InvalidOperationArgs("Expected resource of type PlanDefinition, found " + planDefinitionToRefresh.fhirType());
        }

        if (cqlProcessor.getAllFileInformation().isEmpty()) {
            cqlProcessor.execute();
        }

        try {
            refreshPlanDefinition(planDefinitionToRefresh);

            if (outputPath == null) {
                outputPath = pathToPlanDefinition;
            }

            IOUtils.writeResource(planDefinitionToRefresh, outputPath, IOUtils.Encoding.valueOf(encoding), fhirContext);
        } catch (Exception e) {
            logger.error("Error refreshing measure: {}", pathToPlanDefinition, e);
        }
    }

    public List<IBaseResource> refreshPlanDefinitions(IGUtils.IGInfo igInfo, CqlProcessor cqlProcessor) {
        List<IBaseResource> refreshedPlanDefinitions = new ArrayList<>();
        cqlProcessor.execute();
        if (igInfo.isRefreshPlanDefinitions()) {
            logger.info("Refreshing PlanDefinitions...");
            for (var planDefinition : RefreshUtils.getResourcesOfTypeFromDirectory(fhirContext,
                    "PlanDefinition", igInfo.getPlanDefinitionResourcePath())) {
                refreshedPlanDefinitions.add(refreshPlanDefinition(planDefinition));
            }
            resolvePlanDefinitionPackages();
        }
        return refreshedPlanDefinitions;
    }

    public IBaseResource refreshPlanDefinition(IBaseResource planDefinitionToRefresh) {
        PlanDefinition planDefinition = (PlanDefinition) ResourceAndTypeConverter.convertToR5Resource(fhirContext, planDefinitionToRefresh);
        cqlProcessor.execute();

        logger.info("Refreshing {}", planDefinition.getId());

        RefreshUtils.validatePrimaryLibraryReference(planDefinition);
        String libraryUrl = planDefinition.getLibrary().get(0).getValueAsString();
        LibraryPackage libraryPackage = this.libraryPackages.stream().filter(
                pkg -> libraryUrl.endsWith(pkg.getCqlFileInfo().getIdentifier().getId()
                        )).findAny().orElse(null);
        for (CqlProcessor.CqlSourceFileInformation info : cqlProcessor.getAllFileInformation()) {
            if (libraryUrl.endsWith(info.getIdentifier().getId())) {
                // TODO: should likely verify or resolve/refresh the following elements:
                //  cqfm-artifactComment, cqfm-allocation, cqfm-softwaresystem, url, identifier, version,
                //  name, title, status, experimental, type, publisher, contact, description, useContext,
                //  jurisdiction, and profile(s) (http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/measure-cqfm)
                planDefinition.setDate(new Date());
                DataRequirementsProcessor dataRecProc = new DataRequirementsProcessor();
                Library moduleDefinitionLibrary = getModuleDefinitionLibrary(dataRecProc, info);
                RefreshUtils.cleanModuleDefinitionLibrary(moduleDefinitionLibrary);
                RefreshUtils.refreshCqfmExtensions(planDefinition, moduleDefinitionLibrary);
                RefreshUtils.attachModuleDefinitionLibrary(planDefinition, moduleDefinitionLibrary);
                IBaseResource refreshedPlanDefinition = ResourceAndTypeConverter.
                        convertFromR5Resource(fhirContext, planDefinition);
                this.planDefinitionPackages.add(new PlanDefinitionPackage(planDefinition, refreshedPlanDefinition,
                        fhirContext, libraryPackage));
            }
        }

        logger.info("Success!");
        return planDefinition;
    }

    private List<IBaseResource> activityDefinitions;
    private List<IBaseResource> questionnaires;
    private void resolvePlanDefinitionPackages() {
        // TODO: only resolving definition resources from source IG - enhance to resolve from NPM package.
        //  Additionally need to resolve nested PlanDefinitions
        activityDefinitions = BundleUtil.toListOfResources(fhirContext,
                BundleUtils.getBundleOfResourceTypeFromDirectory(igInfo.getActivityDefinitionResourcePath(),
                        fhirContext, fhirContext.getResourceDefinition("ActivityDefinition")
                                .newInstance().getClass()));
        questionnaires = BundleUtil.toListOfResources(fhirContext,
                BundleUtils.getBundleOfResourceTypeFromDirectory(igInfo.getActivityDefinitionResourcePath(),
                        fhirContext, fhirContext.getResourceDefinition("Questionnaire")
                                .newInstance().getClass()));
        this.planDefinitionPackages.forEach(
                pkg -> pkg.getR5PlanDefinition().getAction().forEach(action -> resolveAction(action, pkg))
        );
    }

    private void resolveAction(PlanDefinition.PlanDefinitionActionComponent action, PlanDefinitionPackage pkg) {
        final IdDt definitionRef;
        if (action.hasDefinitionCanonicalType()) {
            definitionRef = new IdDt(action.getDefinitionCanonicalType().getValueAsString());
        }
        else if (action.hasDefinitionUriType()) {
            definitionRef = new IdDt(action.getDefinitionUriType().getValueAsString());
        }
        else {
            definitionRef = null;
        }
        if (definitionRef != null && definitionRef.hasResourceType()) {
            if (definitionRef.getResourceType().equals("ActivityDefinition")) {
                pkg.addActivityDefinition(
                        activityDefinitions.stream().filter(ad -> ad.getIdElement().getIdPart()
                                .equals(definitionRef.getIdPart())).findFirst().orElse(null)
                );

            }
            else if (definitionRef.getResourceType().equals("Questionnaire")) {
                pkg.addQuestionnaire(
                        questionnaires.stream().filter(q -> q.getIdElement().getIdPart()
                                .equals(definitionRef.getIdPart())).findFirst().orElse(null)
                );
            }
            else {
                logger.warn("Definitions of type {} are not currently supported", definitionRef.getResourceType());
            }
        }
        if (action.hasAction()) {
            action.getAction().forEach(nextAction -> resolveAction(nextAction, pkg));
        }
    }

    private Library getModuleDefinitionLibrary(DataRequirementsProcessor dataRecProc,
                                               CqlProcessor.CqlSourceFileInformation info) {
        return dataRecProc.gatherDataRequirements(
                cqlProcessor.getLibraryManager(),
                cqlProcessor.getLibraryManager().resolveLibrary(
                        info.getIdentifier(), new ArrayList<>()),
                cqlProcessor.getCqlTranslatorOptions().getCqlCompilerOptions(), null, true);
    }

    //

    private String getCqlFromLibrary(Library library) {
        for (var content : library.getContent()) {
            if (content.hasContentType() && content.getContentType().equalsIgnoreCase("text/cql")) {
                return new String(content.getData());
            }
        }
        return null;
    }

    private void refreshContent(Library library, String cql, String elmXml, String elmJson) {
        library.setContent(Arrays.asList(
                new Attachment().setContentType("text/cql").setData(cql.getBytes()),
                new Attachment().setContentType("application/elm+xml").setData(elmXml.getBytes()),
                new Attachment().setContentType("application/elm+json").setData(elmJson.getBytes())));
    }

    public String getPathToPlanDefinition() {
        return pathToPlanDefinition;
    }

    public void setPathToPlanDefinition(String pathToPlanDefinition) {
        this.pathToPlanDefinition = pathToPlanDefinition;
    }

    public String getPathToCql() {
        return pathToCql;
    }

    public void setPathToCql(String pathToCql) {
        this.pathToCql = pathToCql;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public List<PlanDefinitionPackage> getPlanDefinitionPackages() { return planDefinitionPackages; }

}

