package org.opencds.cqf.tooling.operations;

import ca.uhn.fhir.context.FhirContext;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.Library;
import org.opencds.cqf.cql.evaluator.cql2elm.content.InMemoryLibrarySourceProvider;
import org.opencds.cqf.tooling.operations.ig.RefreshIG;
import org.opencds.cqf.tooling.operations.library.LibraryRefresh;
import org.opencds.cqf.tooling.operations.library.LibraryRefreshIT;
import org.opencds.cqf.tooling.utilities.IGUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Objects;

public class PackagingIT {

    private final FhirContext fhirContext = FhirContext.forR5Cached();

    @Test
    void testLibraryRefreshPackaging() {
        IGUtils.IGInfo igInfo = new IGUtils.IGInfo(fhirContext);
        igInfo.setValueSetResourcePath("");
        igInfo.setCodeSystemResourcePath("");
        ImplementationGuide ig = (ImplementationGuide) fhirContext.newXmlParser().parseResource(SIMPLE_IG);
        igInfo.setIgResource(ig);
        ModelManager modelManager = new ModelManager();
        LibraryManager libraryManager = new LibraryManager(modelManager);
        libraryManager.getLibrarySourceLoader().registerProvider(
                new InMemoryLibrarySourceProvider(Collections.singletonList(AOE_CQL_UNCHANGED)));

        Library libraryToRefresh = (Library) fhirContext.newJsonParser().parseResource(AOE_LIBRARY);
        LibraryRefresh libraryRefresh = new LibraryRefresh(fhirContext, igInfo,
                Objects.requireNonNull(LibraryRefreshIT.class.getResource("")).getPath());
        libraryRefresh.setModelManager(modelManager);
        libraryRefresh.setLibraryManager(libraryManager);

        RefreshIG igRefresh = new RefreshIG();

        libraryRefresh.refreshLibrary(libraryToRefresh);

        org.hl7.elm.r1.Library.Includes includes = libraryRefresh.getLibraryPackages().get(0).getCqlFileInfo().getLibrary().getIncludes();
        Assert.assertEquals(includes.getDef().size(), 4);
    }

    private final String AOE_CQL_UNCHANGED = "/*\n" +
            "This example is a work in progress and should not be considered a final specification\n" +
            "or recommendation for guidance. This example will help guide and direct the process\n" +
            "of finding conventions and usage patterns that meet the needs of the various stakeholders\n" +
            "in the measure development community.\n" +
            "\n" +
            "@update: BTR 2020-03-31 ->\n" +
            "Incremented version to 2.0.000\n" +
            "Updated FHIR version to 4.0.1\n" +
            "*/\n" +
            "library AdultOutpatientEncounters version '2.0.000'\n" +
            "\n" +
            "using FHIR version '4.0.1'\n" +
            "\n" +
            "include FHIRHelpers version '4.0.1' called FHIRHelpers\n" +
            "include fhir.cdc.\"opioid-mme-r4\".MMECalculator version '3.0.0' called MMECalculator\n" +
            "\n" +
            "valueset \"Office Visit\": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001'\n" +
            "valueset \"Annual Wellness Visit\": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.526.3.1240'\n" +
            "valueset \"Preventive Care Services - Established Office Visit, 18 and Up\": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1025'\n" +
            "valueset \"Preventive Care Services-Initial Office Visit, 18 and Up\": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1023'\n" +
            "valueset \"Home Healthcare Services\": 'http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1016'\n" +
            "\n" +
            "parameter \"Measurement Period\" Interval<DateTime>\n" +
            "  default Interval[@2019-01-01T00:00:00.0, @2020-01-01T00:00:00.0)\n" +
            "\n" +
            "context Patient\n" +
            "\n" +
            "define \"Qualifying Encounters\":\n" +
            "\t(\n" +
            "    [Encounter: \"Office Visit\"]\n" +
            "  \t\tunion [Encounter: \"Annual Wellness Visit\"]\n" +
            "  \t\tunion [Encounter: \"Preventive Care Services - Established Office Visit, 18 and Up\"]\n" +
            "  \t\tunion [Encounter: \"Preventive Care Services-Initial Office Visit, 18 and Up\"]\n" +
            "  \t\tunion [Encounter: \"Home Healthcare Services\"]\n" +
            "  ) ValidEncounter\n" +
            "\t\twhere ValidEncounter.period during \"Measurement Period\"\n" +
            "  \t\tand ValidEncounter.status  = 'finished'\n";

    private final String AOE_LIBRARY = "{\n" +
            "  \"resourceType\": \"Library\",\n" +
            "  \"id\": \"AdultOutpatientEncounters\",\n" +
            "  \"meta\": {\n" +
            "    \"profile\": [ \"http://hl7.org/fhir/us/cqfmeasures/StructureDefinition/library-cqfm\" ]\n" +
            "  },\n" +
            "  \"url\": \"http://hl7.org/fhir/us/cqfmeasures/Library/AdultOutpatientEncounters\",\n" +
            "  \"identifier\": [ {\n" +
            "    \"use\": \"official\",\n" +
            "    \"system\": \"http://example.org/fhir/cqi/ecqm/Library/Identifier\",\n" +
            "    \"value\": \"AdultOutpatientEncounters\"\n" +
            "  } ],\n" +
            "  \"version\": \"2.0.000\",\n" +
            "  \"name\": \"AdultOutpatientEncounters\",\n" +
            "  \"title\": \"Adult Outpatient Encounters Common Library\",\n" +
            "  \"status\": \"active\",\n" +
            "  \"experimental\": true,\n" +
            "  \"type\": {\n" +
            "    \"coding\": [ {\n" +
            "      \"system\": \"http://terminology.hl7.org/CodeSystem/library-type\",\n" +
            "      \"code\": \"logic-library\"\n" +
            "    } ]\n" +
            "  },\n" +
            "  \"date\": \"2019-09-03\",\n" +
            "  \"publisher\": \"Mathematica\",\n" +
            "  \"description\": \"This library is used as an example in the FHIR Quality Measure Implementation Guide\",\n" +
            "  \"jurisdiction\": [ {\n" +
            "    \"coding\": [ {\n" +
            "      \"system\": \"urn:iso:std:iso:3166\",\n" +
            "      \"code\": \"US\"\n" +
            "    } ]\n" +
            "  } ],\n" +
            "  \"approvalDate\": \"2019-08-03\",\n" +
            "  \"lastReviewDate\": \"2019-08-03\",\n" +
            "  \"relatedArtifact\": [ {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"FHIR model information\",\n" +
            "    \"resource\": \"http://fhir.org/guides/cqf/common/Library/FHIR-ModelInfo|4.0.1\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Library FHIRHelpers\",\n" +
            "    \"resource\": \"http://fhir.org/guides/cqf/common/Library/FHIRHelpers|4.0.1\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Office Visit\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Annual Wellness Visit\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.526.3.1240\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Preventive Care Services - Established Office Visit, 18 and Up\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1025\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Preventive Care Services-Initial Office Visit, 18 and Up\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1023\"\n" +
            "  }, {\n" +
            "    \"type\": \"depends-on\",\n" +
            "    \"display\": \"Value set Home Healthcare Services\",\n" +
            "    \"resource\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1016\"\n" +
            "  } ],\n" +
            "  \"parameter\": [ {\n" +
            "    \"name\": \"Measurement Period\",\n" +
            "    \"use\": \"in\",\n" +
            "    \"min\": 0,\n" +
            "    \"max\": \"1\",\n" +
            "    \"type\": \"Period\"\n" +
            "  }, {\n" +
            "    \"name\": \"Patient\",\n" +
            "    \"use\": \"out\",\n" +
            "    \"min\": 0,\n" +
            "    \"max\": \"1\",\n" +
            "    \"type\": \"Patient\"\n" +
            "  }, {\n" +
            "    \"name\": \"Qualifying Encounters\",\n" +
            "    \"use\": \"out\",\n" +
            "    \"min\": 0,\n" +
            "    \"max\": \"*\",\n" +
            "    \"type\": \"Encounter\"\n" +
            "  } ],\n" +
            "  \"dataRequirement\": [ {\n" +
            "    \"type\": \"Patient\",\n" +
            "    \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Patient\" ]\n" +
            "  }, {\n" +
            "    \"type\": \"Encounter\",\n" +
            "    \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "    \"codeFilter\": [ {\n" +
            "      \"path\": \"type\",\n" +
            "      \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001\"\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"type\": \"Encounter\",\n" +
            "    \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "    \"codeFilter\": [ {\n" +
            "      \"path\": \"type\",\n" +
            "      \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.526.3.1240\"\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"type\": \"Encounter\",\n" +
            "    \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "    \"codeFilter\": [ {\n" +
            "      \"path\": \"type\",\n" +
            "      \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1025\"\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"type\": \"Encounter\",\n" +
            "    \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "    \"codeFilter\": [ {\n" +
            "      \"path\": \"type\",\n" +
            "      \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1023\"\n" +
            "    } ]\n" +
            "  }, {\n" +
            "    \"type\": \"Encounter\",\n" +
            "    \"profile\": [ \"http://hl7.org/fhir/StructureDefinition/Encounter\" ],\n" +
            "    \"codeFilter\": [ {\n" +
            "      \"path\": \"type\",\n" +
            "      \"valueSet\": \"http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1016\"\n" +
            "    } ]\n" +
            "  } ],\n" +
            "  \"content\": [ {\n" +
            "    \"contentType\": \"text/cql\",\n" +
            "    \"data\": \"LyoNClRoaXMgZXhhbXBsZSBpcyBhIHdvcmsgaW4gcHJvZ3Jlc3MgYW5kIHNob3VsZCBub3QgYmUgY29uc2lkZXJlZCBhIGZpbmFsIHNwZWNpZmljYXRpb24NCm9yIHJlY29tbWVuZGF0aW9uIGZvciBndWlkYW5jZS4gVGhpcyBleGFtcGxlIHdpbGwgaGVscCBndWlkZSBhbmQgZGlyZWN0IHRoZSBwcm9jZXNzDQpvZiBmaW5kaW5nIGNvbnZlbnRpb25zIGFuZCB1c2FnZSBwYXR0ZXJucyB0aGF0IG1lZXQgdGhlIG5lZWRzIG9mIHRoZSB2YXJpb3VzIHN0YWtlaG9sZGVycw0KaW4gdGhlIG1lYXN1cmUgZGV2ZWxvcG1lbnQgY29tbXVuaXR5Lg0KDQpAdXBkYXRlOiBCVFIgMjAyMC0wMy0zMSAtPg0KSW5jcmVtZW50ZWQgdmVyc2lvbiB0byAyLjAuMDAwDQpVcGRhdGVkIEZISVIgdmVyc2lvbiB0byA0LjAuMQ0KKi8NCmxpYnJhcnkgQWR1bHRPdXRwYXRpZW50RW5jb3VudGVycyB2ZXJzaW9uICcyLjAuMDAwJw0KDQp1c2luZyBGSElSIHZlcnNpb24gJzQuMC4xJw0KDQppbmNsdWRlIEZISVJIZWxwZXJzIHZlcnNpb24gJzQuMC4xJyBjYWxsZWQgRkhJUkhlbHBlcnMNCg0KdmFsdWVzZXQgIk9mZmljZSBWaXNpdCI6ICdodHRwOi8vY3RzLm5sbS5uaWguZ292L2ZoaXIvVmFsdWVTZXQvMi4xNi44NDAuMS4xMTM4ODMuMy40NjQuMTAwMy4xMDEuMTIuMTAwMScNCnZhbHVlc2V0ICJBbm51YWwgV2VsbG5lc3MgVmlzaXQiOiAnaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNTI2LjMuMTI0MCcNCnZhbHVlc2V0ICJQcmV2ZW50aXZlIENhcmUgU2VydmljZXMgLSBFc3RhYmxpc2hlZCBPZmZpY2UgVmlzaXQsIDE4IGFuZCBVcCI6ICdodHRwOi8vY3RzLm5sbS5uaWguZ292L2ZoaXIvVmFsdWVTZXQvMi4xNi44NDAuMS4xMTM4ODMuMy40NjQuMTAwMy4xMDEuMTIuMTAyNScNCnZhbHVlc2V0ICJQcmV2ZW50aXZlIENhcmUgU2VydmljZXMtSW5pdGlhbCBPZmZpY2UgVmlzaXQsIDE4IGFuZCBVcCI6ICdodHRwOi8vY3RzLm5sbS5uaWguZ292L2ZoaXIvVmFsdWVTZXQvMi4xNi44NDAuMS4xMTM4ODMuMy40NjQuMTAwMy4xMDEuMTIuMTAyMycNCnZhbHVlc2V0ICJIb21lIEhlYWx0aGNhcmUgU2VydmljZXMiOiAnaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNDY0LjEwMDMuMTAxLjEyLjEwMTYnDQoNCnBhcmFtZXRlciAiTWVhc3VyZW1lbnQgUGVyaW9kIiBJbnRlcnZhbDxEYXRlVGltZT4NCiAgZGVmYXVsdCBJbnRlcnZhbFtAMjAxOS0wMS0wMVQwMDowMDowMC4wLCBAMjAyMC0wMS0wMVQwMDowMDowMC4wKQ0KDQpjb250ZXh0IFBhdGllbnQNCg0KZGVmaW5lICJRdWFsaWZ5aW5nIEVuY291bnRlcnMiOg0KCSgNCiAgICBbRW5jb3VudGVyOiAiT2ZmaWNlIFZpc2l0Il0NCiAgCQl1bmlvbiBbRW5jb3VudGVyOiAiQW5udWFsIFdlbGxuZXNzIFZpc2l0Il0NCiAgCQl1bmlvbiBbRW5jb3VudGVyOiAiUHJldmVudGl2ZSBDYXJlIFNlcnZpY2VzIC0gRXN0YWJsaXNoZWQgT2ZmaWNlIFZpc2l0LCAxOCBhbmQgVXAiXQ0KICAJCXVuaW9uIFtFbmNvdW50ZXI6ICJQcmV2ZW50aXZlIENhcmUgU2VydmljZXMtSW5pdGlhbCBPZmZpY2UgVmlzaXQsIDE4IGFuZCBVcCJdDQogIAkJdW5pb24gW0VuY291bnRlcjogIkhvbWUgSGVhbHRoY2FyZSBTZXJ2aWNlcyJdDQogICkgVmFsaWRFbmNvdW50ZXINCgkJd2hlcmUgVmFsaWRFbmNvdW50ZXIucGVyaW9kIGR1cmluZyAiTWVhc3VyZW1lbnQgUGVyaW9kIg0KICAJCWFuZCBWYWxpZEVuY291bnRlci5zdGF0dXMgID0gJ2ZpbmlzaGVkJw0K\"\n" +
            "  }, {\n" +
            "    \"contentType\": \"application/elm+xml\",\n" +
            "    \"data\": \"PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjxsaWJyYXJ5IHhtbG5zPSJ1cm46aGw3LW9yZzplbG06cjEiIHhtbG5zOnQ9InVybjpobDctb3JnOmVsbS10eXBlczpyMSIgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeG1sbnM6eHNkPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgeG1sbnM6Zmhpcj0iaHR0cDovL2hsNy5vcmcvZmhpciIgeG1sbnM6cWRtNDM9InVybjpoZWFsdGhpdC1nb3Y6cWRtOnY0XzMiIHhtbG5zOnFkbTUzPSJ1cm46aGVhbHRoaXQtZ292OnFkbTp2NV8zIiB4bWxuczphPSJ1cm46aGw3LW9yZzpjcWwtYW5ub3RhdGlvbnM6cjEiPg0KICAgPGFubm90YXRpb24gdHJhbnNsYXRvck9wdGlvbnM9IkVuYWJsZUFubm90YXRpb25zLEVuYWJsZUxvY2F0b3JzLERpc2FibGVMaXN0RGVtb3Rpb24sRGlzYWJsZUxpc3RQcm9tb3Rpb24iIHhzaTp0eXBlPSJhOkNxbFRvRWxtSW5mbyIvPg0KICAgPGFubm90YXRpb24geHNpOnR5cGU9ImE6QW5ub3RhdGlvbiI+DQogICAgICA8YTp0IG5hbWU9InVwZGF0ZSIgdmFsdWU9IkJUUiAyMDIwLTAzLTMxIC0+JiN4YTtJbmNyZW1lbnRlZCB2ZXJzaW9uIHRvIDIuMC4wMDAmI3hhO1VwZGF0ZWQgRkhJUiB2ZXJzaW9uIHRvIDQuMC4xIi8+DQogICAgICA8YTpzIHI9IjM0Ij4NCiAgICAgICAgIDxhOnM+LyoKVGhpcyBleGFtcGxlIGlzIGEgd29yayBpbiBwcm9ncmVzcyBhbmQgc2hvdWxkIG5vdCBiZSBjb25zaWRlcmVkIGEgZmluYWwgc3BlY2lmaWNhdGlvbgpvciByZWNvbW1lbmRhdGlvbiBmb3IgZ3VpZGFuY2UuIFRoaXMgZXhhbXBsZSB3aWxsIGhlbHAgZ3VpZGUgYW5kIGRpcmVjdCB0aGUgcHJvY2VzcwpvZiBmaW5kaW5nIGNvbnZlbnRpb25zIGFuZCB1c2FnZSBwYXR0ZXJucyB0aGF0IG1lZXQgdGhlIG5lZWRzIG9mIHRoZSB2YXJpb3VzIHN0YWtlaG9sZGVycwppbiB0aGUgbWVhc3VyZSBkZXZlbG9wbWVudCBjb21tdW5pdHkuCgpAdXBkYXRlOiBCVFIgMjAyMC0wMy0zMSAtPgpJbmNyZW1lbnRlZCB2ZXJzaW9uIHRvIDIuMC4wMDAKVXBkYXRlZCBGSElSIHZlcnNpb24gdG8gNC4wLjEKKi9saWJyYXJ5IEFkdWx0T3V0cGF0aWVudEVuY291bnRlcnMgdmVyc2lvbiAnMi4wLjAwMCc8L2E6cz4NCiAgICAgIDwvYTpzPg0KICAgPC9hbm5vdGF0aW9uPg0KICAgPGlkZW50aWZpZXIgaWQ9IkFkdWx0T3V0cGF0aWVudEVuY291bnRlcnMiIHN5c3RlbT0iaHR0cDovL3NvbWV3aGVyZS5vcmcvZmhpci91di9teWNvbnRlbnRpZyIgdmVyc2lvbj0iMi4wLjAwMCIvPg0KICAgPHNjaGVtYUlkZW50aWZpZXIgaWQ9InVybjpobDctb3JnOmVsbSIgdmVyc2lvbj0icjEiLz4NCiAgIDx1c2luZ3M+DQogICAgICA8ZGVmIGxvY2FsSWRlbnRpZmllcj0iU3lzdGVtIiB1cmk9InVybjpobDctb3JnOmVsbS10eXBlczpyMSIvPg0KICAgICAgPGRlZiBsb2NhbElkPSIxIiBsb2NhdG9yPSIxMzoxLTEzOjI2IiBsb2NhbElkZW50aWZpZXI9IkZISVIiIHVyaT0iaHR0cDovL2hsNy5vcmcvZmhpciIgdmVyc2lvbj0iNC4wLjEiPg0KICAgICAgICAgPGFubm90YXRpb24geHNpOnR5cGU9ImE6QW5ub3RhdGlvbiI+DQogICAgICAgICAgICA8YTpzIHI9IjEiPg0KICAgICAgICAgICAgICAgPGE6cz51c2luZyA8L2E6cz4NCiAgICAgICAgICAgICAgIDxhOnM+DQogICAgICAgICAgICAgICAgICA8YTpzPkZISVI8L2E6cz4NCiAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgPGE6cz4gdmVyc2lvbiAnNC4wLjEnPC9hOnM+DQogICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgIDwvYW5ub3RhdGlvbj4NCiAgICAgIDwvZGVmPg0KICAgPC91c2luZ3M+DQogICA8aW5jbHVkZXM+DQogICAgICA8ZGVmIGxvY2FsSWQ9IjIiIGxvY2F0b3I9IjE1OjEtMTU6NTQiIGxvY2FsSWRlbnRpZmllcj0iRkhJUkhlbHBlcnMiIHBhdGg9Imh0dHA6Ly9maGlyLm9yZy9ndWlkZXMvY3FmL2NvbW1vbi9GSElSSGVscGVycyIgdmVyc2lvbj0iNC4wLjEiPg0KICAgICAgICAgPGFubm90YXRpb24geHNpOnR5cGU9ImE6QW5ub3RhdGlvbiI+DQogICAgICAgICAgICA8YTpzIHI9IjIiPg0KICAgICAgICAgICAgICAgPGE6cz5pbmNsdWRlIDwvYTpzPg0KICAgICAgICAgICAgICAgPGE6cz4NCiAgICAgICAgICAgICAgICAgIDxhOnM+RkhJUkhlbHBlcnM8L2E6cz4NCiAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgPGE6cz4gdmVyc2lvbiAnNC4wLjEnIGNhbGxlZCBGSElSSGVscGVyczwvYTpzPg0KICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICA8L2Fubm90YXRpb24+DQogICAgICA8L2RlZj4NCiAgIDwvaW5jbHVkZXM+DQogICA8cGFyYW1ldGVycz4NCiAgICAgIDxkZWYgbG9jYWxJZD0iMTMiIGxvY2F0b3I9IjIzOjEtMjQ6NjYiIG5hbWU9Ik1lYXN1cmVtZW50IFBlcmlvZCIgYWNjZXNzTGV2ZWw9IlB1YmxpYyI+DQogICAgICAgICA8YW5ub3RhdGlvbiB4c2k6dHlwZT0iYTpBbm5vdGF0aW9uIj4NCiAgICAgICAgICAgIDxhOnMgcj0iMTMiPg0KICAgICAgICAgICAgICAgPGE6cz5wYXJhbWV0ZXIgJnF1b3Q7TWVhc3VyZW1lbnQgUGVyaW9kJnF1b3Q7IDwvYTpzPg0KICAgICAgICAgICAgICAgPGE6cyByPSIxMiI+DQogICAgICAgICAgICAgICAgICA8YTpzPkludGVydmFsJmx0OzwvYTpzPg0KICAgICAgICAgICAgICAgICAgPGE6cyByPSIxMSI+DQogICAgICAgICAgICAgICAgICAgICA8YTpzPkRhdGVUaW1lPC9hOnM+DQogICAgICAgICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgICAgICAgICAgIDxhOnM+PjwvYTpzPg0KICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICA8YTpzPgogIGRlZmF1bHQgPC9hOnM+DQogICAgICAgICAgICAgICA8YTpzIHI9IjEwIj4NCiAgICAgICAgICAgICAgICAgIDxhOnMgcj0iOCI+SW50ZXJ2YWxbQDIwMTktMDEtMDFUMDA6MDA6MDAuMCwgQDIwMjAtMDEtMDFUMDA6MDA6MDAuMCk8L2E6cz4NCiAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICA8L2Fubm90YXRpb24+DQogICAgICAgICA8ZGVmYXVsdCBsb2NhbElkPSIxMCIgbG9jYXRvcj0iMjQ6MTEtMjQ6NjYiIGxvd0Nsb3NlZD0idHJ1ZSIgaGlnaENsb3NlZD0iZmFsc2UiIHhzaTp0eXBlPSJJbnRlcnZhbCI+DQogICAgICAgICAgICA8bG93IGxvY2FsSWQ9IjgiIGxvY2F0b3I9IjI0OjIwLTI0OjQxIiB4c2k6dHlwZT0iRGF0ZVRpbWUiPg0KICAgICAgICAgICAgICAgPHllYXIgdmFsdWVUeXBlPSJ0OkludGVnZXIiIHZhbHVlPSIyMDE5IiB4c2k6dHlwZT0iTGl0ZXJhbCIvPg0KICAgICAgICAgICAgICAgPG1vbnRoIHZhbHVlVHlwZT0idDpJbnRlZ2VyIiB2YWx1ZT0iMSIgeHNpOnR5cGU9IkxpdGVyYWwiLz4NCiAgICAgICAgICAgICAgIDxkYXkgdmFsdWVUeXBlPSJ0OkludGVnZXIiIHZhbHVlPSIxIiB4c2k6dHlwZT0iTGl0ZXJhbCIvPg0KICAgICAgICAgICAgICAgPGhvdXIgdmFsdWVUeXBlPSJ0OkludGVnZXIiIHZhbHVlPSIwIiB4c2k6dHlwZT0iTGl0ZXJhbCIvPg0KICAgICAgICAgICAgICAgPG1pbnV0ZSB2YWx1ZVR5cGU9InQ6SW50ZWdlciIgdmFsdWU9IjAiIHhzaTp0eXBlPSJMaXRlcmFsIi8+DQogICAgICAgICAgICAgICA8c2Vjb25kIHZhbHVlVHlwZT0idDpJbnRlZ2VyIiB2YWx1ZT0iMCIgeHNpOnR5cGU9IkxpdGVyYWwiLz4NCiAgICAgICAgICAgICAgIDxtaWxsaXNlY29uZCB2YWx1ZVR5cGU9InQ6SW50ZWdlciIgdmFsdWU9IjAiIHhzaTp0eXBlPSJMaXRlcmFsIi8+DQogICAgICAgICAgICA8L2xvdz4NCiAgICAgICAgICAgIDxoaWdoIGxvY2FsSWQ9IjkiIGxvY2F0b3I9IjI0OjQ0LTI0OjY1IiB4c2k6dHlwZT0iRGF0ZVRpbWUiPg0KICAgICAgICAgICAgICAgPHllYXIgdmFsdWVUeXBlPSJ0OkludGVnZXIiIHZhbHVlPSIyMDIwIiB4c2k6dHlwZT0iTGl0ZXJhbCIvPg0KICAgICAgICAgICAgICAgPG1vbnRoIHZhbHVlVHlwZT0idDpJbnRlZ2VyIiB2YWx1ZT0iMSIgeHNpOnR5cGU9IkxpdGVyYWwiLz4NCiAgICAgICAgICAgICAgIDxkYXkgdmFsdWVUeXBlPSJ0OkludGVnZXIiIHZhbHVlPSIxIiB4c2k6dHlwZT0iTGl0ZXJhbCIvPg0KICAgICAgICAgICAgICAgPGhvdXIgdmFsdWVUeXBlPSJ0OkludGVnZXIiIHZhbHVlPSIwIiB4c2k6dHlwZT0iTGl0ZXJhbCIvPg0KICAgICAgICAgICAgICAgPG1pbnV0ZSB2YWx1ZVR5cGU9InQ6SW50ZWdlciIgdmFsdWU9IjAiIHhzaTp0eXBlPSJMaXRlcmFsIi8+DQogICAgICAgICAgICAgICA8c2Vjb25kIHZhbHVlVHlwZT0idDpJbnRlZ2VyIiB2YWx1ZT0iMCIgeHNpOnR5cGU9IkxpdGVyYWwiLz4NCiAgICAgICAgICAgICAgIDxtaWxsaXNlY29uZCB2YWx1ZVR5cGU9InQ6SW50ZWdlciIgdmFsdWU9IjAiIHhzaTp0eXBlPSJMaXRlcmFsIi8+DQogICAgICAgICAgICA8L2hpZ2g+DQogICAgICAgICA8L2RlZmF1bHQ+DQogICAgICAgICA8cGFyYW1ldGVyVHlwZVNwZWNpZmllciBsb2NhbElkPSIxMiIgbG9jYXRvcj0iMjM6MzItMjM6NDkiIHhzaTp0eXBlPSJJbnRlcnZhbFR5cGVTcGVjaWZpZXIiPg0KICAgICAgICAgICAgPHBvaW50VHlwZSBsb2NhbElkPSIxMSIgbG9jYXRvcj0iMjM6NDEtMjM6NDgiIG5hbWU9InQ6RGF0ZVRpbWUiIHhzaTp0eXBlPSJOYW1lZFR5cGVTcGVjaWZpZXIiLz4NCiAgICAgICAgIDwvcGFyYW1ldGVyVHlwZVNwZWNpZmllcj4NCiAgICAgIDwvZGVmPg0KICAgPC9wYXJhbWV0ZXJzPg0KICAgPHZhbHVlU2V0cz4NCiAgICAgIDxkZWYgbG9jYWxJZD0iMyIgbG9jYXRvcj0iMTc6MS0xNzoxMDQiIG5hbWU9Ik9mZmljZSBWaXNpdCIgaWQ9Imh0dHA6Ly9jdHMubmxtLm5paC5nb3YvZmhpci9WYWx1ZVNldC8yLjE2Ljg0MC4xLjExMzg4My4zLjQ2NC4xMDAzLjEwMS4xMi4xMDAxIiBhY2Nlc3NMZXZlbD0iUHVibGljIj4NCiAgICAgICAgIDxhbm5vdGF0aW9uIHhzaTp0eXBlPSJhOkFubm90YXRpb24iPg0KICAgICAgICAgICAgPGE6cyByPSIzIj4NCiAgICAgICAgICAgICAgIDxhOnM+dmFsdWVzZXQgJnF1b3Q7T2ZmaWNlIFZpc2l0JnF1b3Q7OiAnaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNDY0LjEwMDMuMTAxLjEyLjEwMDEnPC9hOnM+DQogICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgIDwvYW5ub3RhdGlvbj4NCiAgICAgIDwvZGVmPg0KICAgICAgPGRlZiBsb2NhbElkPSI0IiBsb2NhdG9yPSIxODoxLTE4OjEwMyIgbmFtZT0iQW5udWFsIFdlbGxuZXNzIFZpc2l0IiBpZD0iaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNTI2LjMuMTI0MCIgYWNjZXNzTGV2ZWw9IlB1YmxpYyI+DQogICAgICAgICA8YW5ub3RhdGlvbiB4c2k6dHlwZT0iYTpBbm5vdGF0aW9uIj4NCiAgICAgICAgICAgIDxhOnMgcj0iNCI+DQogICAgICAgICAgICAgICA8YTpzPnZhbHVlc2V0ICZxdW90O0FubnVhbCBXZWxsbmVzcyBWaXNpdCZxdW90OzogJ2h0dHA6Ly9jdHMubmxtLm5paC5nb3YvZmhpci9WYWx1ZVNldC8yLjE2Ljg0MC4xLjExMzg4My4zLjUyNi4zLjEyNDAnPC9hOnM+DQogICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgIDwvYW5ub3RhdGlvbj4NCiAgICAgIDwvZGVmPg0KICAgICAgPGRlZiBsb2NhbElkPSI1IiBsb2NhdG9yPSIxOToxLTE5OjE1NCIgbmFtZT0iUHJldmVudGl2ZSBDYXJlIFNlcnZpY2VzIC0gRXN0YWJsaXNoZWQgT2ZmaWNlIFZpc2l0LCAxOCBhbmQgVXAiIGlkPSJodHRwOi8vY3RzLm5sbS5uaWguZ292L2ZoaXIvVmFsdWVTZXQvMi4xNi44NDAuMS4xMTM4ODMuMy40NjQuMTAwMy4xMDEuMTIuMTAyNSIgYWNjZXNzTGV2ZWw9IlB1YmxpYyI+DQogICAgICAgICA8YW5ub3RhdGlvbiB4c2k6dHlwZT0iYTpBbm5vdGF0aW9uIj4NCiAgICAgICAgICAgIDxhOnMgcj0iNSI+DQogICAgICAgICAgICAgICA8YTpzPnZhbHVlc2V0ICZxdW90O1ByZXZlbnRpdmUgQ2FyZSBTZXJ2aWNlcyAtIEVzdGFibGlzaGVkIE9mZmljZSBWaXNpdCwgMTggYW5kIFVwJnF1b3Q7OiAnaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNDY0LjEwMDMuMTAxLjEyLjEwMjUnPC9hOnM+DQogICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgIDwvYW5ub3RhdGlvbj4NCiAgICAgIDwvZGVmPg0KICAgICAgPGRlZiBsb2NhbElkPSI2IiBsb2NhdG9yPSIyMDoxLTIwOjE0OCIgbmFtZT0iUHJldmVudGl2ZSBDYXJlIFNlcnZpY2VzLUluaXRpYWwgT2ZmaWNlIFZpc2l0LCAxOCBhbmQgVXAiIGlkPSJodHRwOi8vY3RzLm5sbS5uaWguZ292L2ZoaXIvVmFsdWVTZXQvMi4xNi44NDAuMS4xMTM4ODMuMy40NjQuMTAwMy4xMDEuMTIuMTAyMyIgYWNjZXNzTGV2ZWw9IlB1YmxpYyI+DQogICAgICAgICA8YW5ub3RhdGlvbiB4c2k6dHlwZT0iYTpBbm5vdGF0aW9uIj4NCiAgICAgICAgICAgIDxhOnMgcj0iNiI+DQogICAgICAgICAgICAgICA8YTpzPnZhbHVlc2V0ICZxdW90O1ByZXZlbnRpdmUgQ2FyZSBTZXJ2aWNlcy1Jbml0aWFsIE9mZmljZSBWaXNpdCwgMTggYW5kIFVwJnF1b3Q7OiAnaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNDY0LjEwMDMuMTAxLjEyLjEwMjMnPC9hOnM+DQogICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgIDwvYW5ub3RhdGlvbj4NCiAgICAgIDwvZGVmPg0KICAgICAgPGRlZiBsb2NhbElkPSI3IiBsb2NhdG9yPSIyMToxLTIxOjExNiIgbmFtZT0iSG9tZSBIZWFsdGhjYXJlIFNlcnZpY2VzIiBpZD0iaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNDY0LjEwMDMuMTAxLjEyLjEwMTYiIGFjY2Vzc0xldmVsPSJQdWJsaWMiPg0KICAgICAgICAgPGFubm90YXRpb24geHNpOnR5cGU9ImE6QW5ub3RhdGlvbiI+DQogICAgICAgICAgICA8YTpzIHI9IjciPg0KICAgICAgICAgICAgICAgPGE6cz52YWx1ZXNldCAmcXVvdDtIb21lIEhlYWx0aGNhcmUgU2VydmljZXMmcXVvdDs6ICdodHRwOi8vY3RzLm5sbS5uaWguZ292L2ZoaXIvVmFsdWVTZXQvMi4xNi44NDAuMS4xMTM4ODMuMy40NjQuMTAwMy4xMDEuMTIuMTAxNic8L2E6cz4NCiAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgPC9hbm5vdGF0aW9uPg0KICAgICAgPC9kZWY+DQogICA8L3ZhbHVlU2V0cz4NCiAgIDxjb250ZXh0cz4NCiAgICAgIDxkZWYgbG9jYXRvcj0iMjY6MS0yNjoxNSIgbmFtZT0iUGF0aWVudCIvPg0KICAgPC9jb250ZXh0cz4NCiAgIDxzdGF0ZW1lbnRzPg0KICAgICAgPGRlZiBsb2NhdG9yPSIyNjoxLTI2OjE1IiBuYW1lPSJQYXRpZW50IiBjb250ZXh0PSJQYXRpZW50Ij4NCiAgICAgICAgIDxleHByZXNzaW9uIHhzaTp0eXBlPSJTaW5nbGV0b25Gcm9tIj4NCiAgICAgICAgICAgIDxvcGVyYW5kIGxvY2F0b3I9IjI2OjEtMjY6MTUiIGRhdGFUeXBlPSJmaGlyOlBhdGllbnQiIHRlbXBsYXRlSWQ9Imh0dHA6Ly9obDcub3JnL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9QYXRpZW50IiB4c2k6dHlwZT0iUmV0cmlldmUiLz4NCiAgICAgICAgIDwvZXhwcmVzc2lvbj4NCiAgICAgIDwvZGVmPg0KICAgICAgPGRlZiBsb2NhbElkPSIzNCIgbG9jYXRvcj0iMjg6MS0zNzo0MyIgbmFtZT0iUXVhbGlmeWluZyBFbmNvdW50ZXJzIiBjb250ZXh0PSJQYXRpZW50IiBhY2Nlc3NMZXZlbD0iUHVibGljIj4NCiAgICAgICAgIDxhbm5vdGF0aW9uIHhzaTp0eXBlPSJhOkFubm90YXRpb24iPg0KICAgICAgICAgICAgPGE6cyByPSIzNCI+DQogICAgICAgICAgICAgICA8YTpzPmRlZmluZSAmcXVvdDtRdWFsaWZ5aW5nIEVuY291bnRlcnMmcXVvdDs6Cgk8L2E6cz4NCiAgICAgICAgICAgICAgIDxhOnMgcj0iMzMiPg0KICAgICAgICAgICAgICAgICAgPGE6cz4NCiAgICAgICAgICAgICAgICAgICAgIDxhOnMgcj0iMjMiPg0KICAgICAgICAgICAgICAgICAgICAgICAgPGE6cyByPSIyMiI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPigKICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cyByPSIyMiI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzIHI9IjIwIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnMgcj0iMTgiPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cyByPSIxNiI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzIHI9IjE0Ij4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+W0VuY291bnRlcjogPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz4mcXVvdDtPZmZpY2UgVmlzaXQmcXVvdDs8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz5dPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+CiAgCQl1bmlvbiA8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnMgcj0iMTUiPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz5bRW5jb3VudGVyOiA8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPiZxdW90O0FubnVhbCBXZWxsbmVzcyBWaXNpdCZxdW90OzwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPl08L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPgogIAkJdW5pb24gPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzIHI9IjE3Ij4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+W0VuY291bnRlcjogPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz4mcXVvdDtQcmV2ZW50aXZlIENhcmUgU2VydmljZXMgLSBFc3RhYmxpc2hlZCBPZmZpY2UgVmlzaXQsIDE4IGFuZCBVcCZxdW90OzwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPl08L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPgogIAkJdW5pb24gPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzIHI9IjE5Ij4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+W0VuY291bnRlcjogPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz4mcXVvdDtQcmV2ZW50aXZlIENhcmUgU2VydmljZXMtSW5pdGlhbCBPZmZpY2UgVmlzaXQsIDE4IGFuZCBVcCZxdW90OzwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPl08L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPgogIAkJdW5pb24gPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzIHI9IjIxIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+W0VuY291bnRlcjogPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz4mcXVvdDtIb21lIEhlYWx0aGNhcmUgU2VydmljZXMmcXVvdDs8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz5dPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz4KICApPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+IFZhbGlkRW5jb3VudGVyPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgPGE6cz4KCQk8L2E6cz4NCiAgICAgICAgICAgICAgICAgIDxhOnMgcj0iMzIiPg0KICAgICAgICAgICAgICAgICAgICAgPGE6cz53aGVyZSA8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgIDxhOnMgcj0iMzIiPg0KICAgICAgICAgICAgICAgICAgICAgICAgPGE6cyByPSIyNyI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzIHI9IjI1Ij4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnMgcj0iMjQiPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz5WYWxpZEVuY291bnRlcjwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPi48L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnMgcj0iMjUiPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cz5wZXJpb2Q8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzIHI9IjI3Ij4gZHVyaW5nIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cyByPSIyNiI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPiZxdW90O01lYXN1cmVtZW50IFBlcmlvZCZxdW90OzwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+CiAgCQlhbmQgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICA8YTpzIHI9IjMxIj4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnMgcj0iMjkiPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cyByPSIyOCI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPlZhbGlkRW5jb3VudGVyPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+LjwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cyByPSIyOSI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPnN0YXR1czwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgIDxhOnM+ICA9IDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgPGE6cyByPSIzMCI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8YTpzPidmaW5pc2hlZCc8L2E6cz4NCiAgICAgICAgICAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgICAgICAgICAgIDwvYTpzPg0KICAgICAgICAgICAgICAgPC9hOnM+DQogICAgICAgICAgICA8L2E6cz4NCiAgICAgICAgIDwvYW5ub3RhdGlvbj4NCiAgICAgICAgIDxleHByZXNzaW9uIGxvY2FsSWQ9IjMzIiBsb2NhdG9yPSIyOToyLTM3OjQzIiB4c2k6dHlwZT0iUXVlcnkiPg0KICAgICAgICAgICAgPHNvdXJjZSBsb2NhbElkPSIyMyIgbG9jYXRvcj0iMjk6Mi0zNToxOCIgYWxpYXM9IlZhbGlkRW5jb3VudGVyIj4NCiAgICAgICAgICAgICAgIDxleHByZXNzaW9uIGxvY2FsSWQ9IjIyIiBsb2NhdG9yPSIyOToyLTM1OjMiIHhzaTp0eXBlPSJVbmlvbiI+DQogICAgICAgICAgICAgICAgICA8b3BlcmFuZCBsb2NhbElkPSIyMCIgbG9jYXRvcj0iMzA6NS0zMzo4MSIgeHNpOnR5cGU9IlVuaW9uIj4NCiAgICAgICAgICAgICAgICAgICAgIDxvcGVyYW5kIGxvY2FsSWQ9IjE2IiBsb2NhdG9yPSIzMDo1LTMxOjQ2IiB4c2k6dHlwZT0iVW5pb24iPg0KICAgICAgICAgICAgICAgICAgICAgICAgPG9wZXJhbmQgbG9jYWxJZD0iMTQiIGxvY2F0b3I9IjMwOjUtMzA6MzEiIGRhdGFUeXBlPSJmaGlyOkVuY291bnRlciIgdGVtcGxhdGVJZD0iaHR0cDovL2hsNy5vcmcvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL0VuY291bnRlciIgY29kZVByb3BlcnR5PSJ0eXBlIiBjb2RlQ29tcGFyYXRvcj0iaW4iIHhzaTp0eXBlPSJSZXRyaWV2ZSI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICA8Y29kZXMgbG9jYXRvcj0iMzA6MTctMzA6MzAiIG5hbWU9Ik9mZmljZSBWaXNpdCIgeHNpOnR5cGU9IlZhbHVlU2V0UmVmIi8+DQogICAgICAgICAgICAgICAgICAgICAgICA8L29wZXJhbmQ+DQogICAgICAgICAgICAgICAgICAgICAgICA8b3BlcmFuZCBsb2NhbElkPSIxNSIgbG9jYXRvcj0iMzE6MTEtMzE6NDYiIGRhdGFUeXBlPSJmaGlyOkVuY291bnRlciIgdGVtcGxhdGVJZD0iaHR0cDovL2hsNy5vcmcvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL0VuY291bnRlciIgY29kZVByb3BlcnR5PSJ0eXBlIiBjb2RlQ29tcGFyYXRvcj0iaW4iIHhzaTp0eXBlPSJSZXRyaWV2ZSI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICA8Y29kZXMgbG9jYXRvcj0iMzE6MjMtMzE6NDUiIG5hbWU9IkFubnVhbCBXZWxsbmVzcyBWaXNpdCIgeHNpOnR5cGU9IlZhbHVlU2V0UmVmIi8+DQogICAgICAgICAgICAgICAgICAgICAgICA8L29wZXJhbmQ+DQogICAgICAgICAgICAgICAgICAgICA8L29wZXJhbmQ+DQogICAgICAgICAgICAgICAgICAgICA8b3BlcmFuZCB4c2k6dHlwZT0iVW5pb24iPg0KICAgICAgICAgICAgICAgICAgICAgICAgPG9wZXJhbmQgbG9jYWxJZD0iMTciIGxvY2F0b3I9IjMyOjExLTMyOjg3IiBkYXRhVHlwZT0iZmhpcjpFbmNvdW50ZXIiIHRlbXBsYXRlSWQ9Imh0dHA6Ly9obDcub3JnL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9FbmNvdW50ZXIiIGNvZGVQcm9wZXJ0eT0idHlwZSIgY29kZUNvbXBhcmF0b3I9ImluIiB4c2k6dHlwZT0iUmV0cmlldmUiPg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgPGNvZGVzIGxvY2F0b3I9IjMyOjIzLTMyOjg2IiBuYW1lPSJQcmV2ZW50aXZlIENhcmUgU2VydmljZXMgLSBFc3RhYmxpc2hlZCBPZmZpY2UgVmlzaXQsIDE4IGFuZCBVcCIgeHNpOnR5cGU9IlZhbHVlU2V0UmVmIi8+DQogICAgICAgICAgICAgICAgICAgICAgICA8L29wZXJhbmQ+DQogICAgICAgICAgICAgICAgICAgICAgICA8b3BlcmFuZCBsb2NhbElkPSIxOSIgbG9jYXRvcj0iMzM6MTEtMzM6ODEiIGRhdGFUeXBlPSJmaGlyOkVuY291bnRlciIgdGVtcGxhdGVJZD0iaHR0cDovL2hsNy5vcmcvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL0VuY291bnRlciIgY29kZVByb3BlcnR5PSJ0eXBlIiBjb2RlQ29tcGFyYXRvcj0iaW4iIHhzaTp0eXBlPSJSZXRyaWV2ZSI+DQogICAgICAgICAgICAgICAgICAgICAgICAgICA8Y29kZXMgbG9jYXRvcj0iMzM6MjMtMzM6ODAiIG5hbWU9IlByZXZlbnRpdmUgQ2FyZSBTZXJ2aWNlcy1Jbml0aWFsIE9mZmljZSBWaXNpdCwgMTggYW5kIFVwIiB4c2k6dHlwZT0iVmFsdWVTZXRSZWYiLz4NCiAgICAgICAgICAgICAgICAgICAgICAgIDwvb3BlcmFuZD4NCiAgICAgICAgICAgICAgICAgICAgIDwvb3BlcmFuZD4NCiAgICAgICAgICAgICAgICAgIDwvb3BlcmFuZD4NCiAgICAgICAgICAgICAgICAgIDxvcGVyYW5kIGxvY2FsSWQ9IjIxIiBsb2NhdG9yPSIzNDoxMS0zNDo0OSIgZGF0YVR5cGU9ImZoaXI6RW5jb3VudGVyIiB0ZW1wbGF0ZUlkPSJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vRW5jb3VudGVyIiBjb2RlUHJvcGVydHk9InR5cGUiIGNvZGVDb21wYXJhdG9yPSJpbiIgeHNpOnR5cGU9IlJldHJpZXZlIj4NCiAgICAgICAgICAgICAgICAgICAgIDxjb2RlcyBsb2NhdG9yPSIzNDoyMy0zNDo0OCIgbmFtZT0iSG9tZSBIZWFsdGhjYXJlIFNlcnZpY2VzIiB4c2k6dHlwZT0iVmFsdWVTZXRSZWYiLz4NCiAgICAgICAgICAgICAgICAgIDwvb3BlcmFuZD4NCiAgICAgICAgICAgICAgIDwvZXhwcmVzc2lvbj4NCiAgICAgICAgICAgIDwvc291cmNlPg0KICAgICAgICAgICAgPHdoZXJlIGxvY2FsSWQ9IjMyIiBsb2NhdG9yPSIzNjozLTM3OjQzIiB4c2k6dHlwZT0iQW5kIj4NCiAgICAgICAgICAgICAgIDxvcGVyYW5kIGxvY2FsSWQ9IjI3IiBsb2NhdG9yPSIzNjo5LTM2OjU3IiB4c2k6dHlwZT0iSW5jbHVkZWRJbiI+DQogICAgICAgICAgICAgICAgICA8b3BlcmFuZCBuYW1lPSJUb0ludGVydmFsIiBsaWJyYXJ5TmFtZT0iRkhJUkhlbHBlcnMiIHhzaTp0eXBlPSJGdW5jdGlvblJlZiI+DQogICAgICAgICAgICAgICAgICAgICA8b3BlcmFuZCBsb2NhbElkPSIyNSIgbG9jYXRvcj0iMzY6OS0zNjoyOSIgcGF0aD0icGVyaW9kIiBzY29wZT0iVmFsaWRFbmNvdW50ZXIiIHhzaTp0eXBlPSJQcm9wZXJ0eSIvPg0KICAgICAgICAgICAgICAgICAgPC9vcGVyYW5kPg0KICAgICAgICAgICAgICAgICAgPG9wZXJhbmQgbG9jYWxJZD0iMjYiIGxvY2F0b3I9IjM2OjM4LTM2OjU3IiBuYW1lPSJNZWFzdXJlbWVudCBQZXJpb2QiIHhzaTp0eXBlPSJQYXJhbWV0ZXJSZWYiLz4NCiAgICAgICAgICAgICAgIDwvb3BlcmFuZD4NCiAgICAgICAgICAgICAgIDxvcGVyYW5kIGxvY2FsSWQ9IjMxIiBsb2NhdG9yPSIzNzo5LTM3OjQzIiB4c2k6dHlwZT0iRXF1YWwiPg0KICAgICAgICAgICAgICAgICAgPG9wZXJhbmQgbmFtZT0iVG9TdHJpbmciIGxpYnJhcnlOYW1lPSJGSElSSGVscGVycyIgeHNpOnR5cGU9IkZ1bmN0aW9uUmVmIj4NCiAgICAgICAgICAgICAgICAgICAgIDxvcGVyYW5kIGxvY2FsSWQ9IjI5IiBsb2NhdG9yPSIzNzo5LTM3OjI5IiBwYXRoPSJzdGF0dXMiIHNjb3BlPSJWYWxpZEVuY291bnRlciIgeHNpOnR5cGU9IlByb3BlcnR5Ii8+DQogICAgICAgICAgICAgICAgICA8L29wZXJhbmQ+DQogICAgICAgICAgICAgICAgICA8b3BlcmFuZCBsb2NhbElkPSIzMCIgbG9jYXRvcj0iMzc6MzQtMzc6NDMiIHZhbHVlVHlwZT0idDpTdHJpbmciIHZhbHVlPSJmaW5pc2hlZCIgeHNpOnR5cGU9IkxpdGVyYWwiLz4NCiAgICAgICAgICAgICAgIDwvb3BlcmFuZD4NCiAgICAgICAgICAgIDwvd2hlcmU+DQogICAgICAgICA8L2V4cHJlc3Npb24+DQogICAgICA8L2RlZj4NCiAgIDwvc3RhdGVtZW50cz4NCjwvbGlicmFyeT4NCg==\"\n" +
            "  }, {\n" +
            "    \"contentType\": \"application/elm+json\",\n" +
            "    \"data\": \"ew0KICAgImxpYnJhcnkiIDogew0KICAgICAgImFubm90YXRpb24iIDogWyB7DQogICAgICAgICAidHJhbnNsYXRvck9wdGlvbnMiIDogIkVuYWJsZUFubm90YXRpb25zLEVuYWJsZUxvY2F0b3JzLERpc2FibGVMaXN0RGVtb3Rpb24sRGlzYWJsZUxpc3RQcm9tb3Rpb24iLA0KICAgICAgICAgInR5cGUiIDogIkNxbFRvRWxtSW5mbyINCiAgICAgIH0sIHsNCiAgICAgICAgICJ0eXBlIiA6ICJBbm5vdGF0aW9uIiwNCiAgICAgICAgICJ0IiA6IFsgew0KICAgICAgICAgICAgIm5hbWUiIDogInVwZGF0ZSIsDQogICAgICAgICAgICAidmFsdWUiIDogIkJUUiAyMDIwLTAzLTMxIC0+XG5JbmNyZW1lbnRlZCB2ZXJzaW9uIHRvIDIuMC4wMDBcblVwZGF0ZWQgRkhJUiB2ZXJzaW9uIHRvIDQuMC4xIg0KICAgICAgICAgfSBdLA0KICAgICAgICAgInMiIDogew0KICAgICAgICAgICAgInIiIDogIjM0IiwNCiAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIi8qXG5UaGlzIGV4YW1wbGUgaXMgYSB3b3JrIGluIHByb2dyZXNzIGFuZCBzaG91bGQgbm90IGJlIGNvbnNpZGVyZWQgYSBmaW5hbCBzcGVjaWZpY2F0aW9uXG5vciByZWNvbW1lbmRhdGlvbiBmb3IgZ3VpZGFuY2UuIFRoaXMgZXhhbXBsZSB3aWxsIGhlbHAgZ3VpZGUgYW5kIGRpcmVjdCB0aGUgcHJvY2Vzc1xub2YgZmluZGluZyBjb252ZW50aW9ucyBhbmQgdXNhZ2UgcGF0dGVybnMgdGhhdCBtZWV0IHRoZSBuZWVkcyBvZiB0aGUgdmFyaW91cyBzdGFrZWhvbGRlcnNcbmluIHRoZSBtZWFzdXJlIGRldmVsb3BtZW50IGNvbW11bml0eS5cblxuQHVwZGF0ZTogQlRSIDIwMjAtMDMtMzEgLT5cbkluY3JlbWVudGVkIHZlcnNpb24gdG8gMi4wLjAwMFxuVXBkYXRlZCBGSElSIHZlcnNpb24gdG8gNC4wLjFcbiovIiwibGlicmFyeSBBZHVsdE91dHBhdGllbnRFbmNvdW50ZXJzIHZlcnNpb24gJzIuMC4wMDAnIiBdDQogICAgICAgICAgICB9IF0NCiAgICAgICAgIH0NCiAgICAgIH0gXSwNCiAgICAgICJpZGVudGlmaWVyIiA6IHsNCiAgICAgICAgICJpZCIgOiAiQWR1bHRPdXRwYXRpZW50RW5jb3VudGVycyIsDQogICAgICAgICAic3lzdGVtIiA6ICJodHRwOi8vc29tZXdoZXJlLm9yZy9maGlyL3V2L215Y29udGVudGlnIiwNCiAgICAgICAgICJ2ZXJzaW9uIiA6ICIyLjAuMDAwIg0KICAgICAgfSwNCiAgICAgICJzY2hlbWFJZGVudGlmaWVyIiA6IHsNCiAgICAgICAgICJpZCIgOiAidXJuOmhsNy1vcmc6ZWxtIiwNCiAgICAgICAgICJ2ZXJzaW9uIiA6ICJyMSINCiAgICAgIH0sDQogICAgICAidXNpbmdzIiA6IHsNCiAgICAgICAgICJkZWYiIDogWyB7DQogICAgICAgICAgICAibG9jYWxJZGVudGlmaWVyIiA6ICJTeXN0ZW0iLA0KICAgICAgICAgICAgInVyaSIgOiAidXJuOmhsNy1vcmc6ZWxtLXR5cGVzOnIxIg0KICAgICAgICAgfSwgew0KICAgICAgICAgICAgImxvY2FsSWQiIDogIjEiLA0KICAgICAgICAgICAgImxvY2F0b3IiIDogIjEzOjEtMTM6MjYiLA0KICAgICAgICAgICAgImxvY2FsSWRlbnRpZmllciIgOiAiRkhJUiIsDQogICAgICAgICAgICAidXJpIiA6ICJodHRwOi8vaGw3Lm9yZy9maGlyIiwNCiAgICAgICAgICAgICJ2ZXJzaW9uIiA6ICI0LjAuMSIsDQogICAgICAgICAgICAiYW5ub3RhdGlvbiIgOiBbIHsNCiAgICAgICAgICAgICAgICJ0eXBlIiA6ICJBbm5vdGF0aW9uIiwNCiAgICAgICAgICAgICAgICJzIiA6IHsNCiAgICAgICAgICAgICAgICAgICJyIiA6ICIxIiwNCiAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIiIsInVzaW5nICIgXQ0KICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiRkhJUiIgXQ0KICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiIHZlcnNpb24gIiwiJzQuMC4xJyIgXQ0KICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICB9DQogICAgICAgICAgICB9IF0NCiAgICAgICAgIH0gXQ0KICAgICAgfSwNCiAgICAgICJpbmNsdWRlcyIgOiB7DQogICAgICAgICAiZGVmIiA6IFsgew0KICAgICAgICAgICAgImxvY2FsSWQiIDogIjIiLA0KICAgICAgICAgICAgImxvY2F0b3IiIDogIjE1OjEtMTU6NTQiLA0KICAgICAgICAgICAgImxvY2FsSWRlbnRpZmllciIgOiAiRkhJUkhlbHBlcnMiLA0KICAgICAgICAgICAgInBhdGgiIDogImh0dHA6Ly9maGlyLm9yZy9ndWlkZXMvY3FmL2NvbW1vbi9GSElSSGVscGVycyIsDQogICAgICAgICAgICAidmVyc2lvbiIgOiAiNC4wLjEiLA0KICAgICAgICAgICAgImFubm90YXRpb24iIDogWyB7DQogICAgICAgICAgICAgICAidHlwZSIgOiAiQW5ub3RhdGlvbiIsDQogICAgICAgICAgICAgICAicyIgOiB7DQogICAgICAgICAgICAgICAgICAiciIgOiAiMiIsDQogICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICIiLCJpbmNsdWRlICIgXQ0KICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiRkhJUkhlbHBlcnMiIF0NCiAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIiB2ZXJzaW9uICIsIic0LjAuMSciLCIgY2FsbGVkICIsIkZISVJIZWxwZXJzIiBdDQogICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgIH0gXQ0KICAgICAgICAgfSBdDQogICAgICB9LA0KICAgICAgInBhcmFtZXRlcnMiIDogew0KICAgICAgICAgImRlZiIgOiBbIHsNCiAgICAgICAgICAgICJsb2NhbElkIiA6ICIxMyIsDQogICAgICAgICAgICAibG9jYXRvciIgOiAiMjM6MS0yNDo2NiIsDQogICAgICAgICAgICAibmFtZSIgOiAiTWVhc3VyZW1lbnQgUGVyaW9kIiwNCiAgICAgICAgICAgICJhY2Nlc3NMZXZlbCIgOiAiUHVibGljIiwNCiAgICAgICAgICAgICJhbm5vdGF0aW9uIiA6IFsgew0KICAgICAgICAgICAgICAgInR5cGUiIDogIkFubm90YXRpb24iLA0KICAgICAgICAgICAgICAgInMiIDogew0KICAgICAgICAgICAgICAgICAgInIiIDogIjEzIiwNCiAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIiIsInBhcmFtZXRlciAiLCJcIk1lYXN1cmVtZW50IFBlcmlvZFwiIiwiICIgXQ0KICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgInIiIDogIjEyIiwNCiAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIkludGVydmFsPCIgXQ0KICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjExIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIkRhdGVUaW1lIiBdDQogICAgICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICI+IiBdDQogICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJcbiAgZGVmYXVsdCAiIF0NCiAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICJyIiA6ICIxMCIsDQogICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICJyIiA6ICI4IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJJbnRlcnZhbFsiLCJAMjAxOS0wMS0wMVQwMDowMDowMC4wIiwiLCAiLCJAMjAyMC0wMS0wMVQwMDowMDowMC4wIiwiKSIgXQ0KICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgIH0gXSwNCiAgICAgICAgICAgICJkZWZhdWx0IiA6IHsNCiAgICAgICAgICAgICAgICJsb2NhbElkIiA6ICIxMCIsDQogICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMjQ6MTEtMjQ6NjYiLA0KICAgICAgICAgICAgICAgImxvd0Nsb3NlZCIgOiB0cnVlLA0KICAgICAgICAgICAgICAgImhpZ2hDbG9zZWQiIDogZmFsc2UsDQogICAgICAgICAgICAgICAidHlwZSIgOiAiSW50ZXJ2YWwiLA0KICAgICAgICAgICAgICAgImxvdyIgOiB7DQogICAgICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiOCIsDQogICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMjQ6MjAtMjQ6NDEiLA0KICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkRhdGVUaW1lIiwNCiAgICAgICAgICAgICAgICAgICJ5ZWFyIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZVR5cGUiIDogInt1cm46aGw3LW9yZzplbG0tdHlwZXM6cjF9SW50ZWdlciIsDQogICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogIjIwMTkiLA0KICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkxpdGVyYWwiDQogICAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgICAgIm1vbnRoIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZVR5cGUiIDogInt1cm46aGw3LW9yZzplbG0tdHlwZXM6cjF9SW50ZWdlciIsDQogICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogIjEiLA0KICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkxpdGVyYWwiDQogICAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgICAgImRheSIgOiB7DQogICAgICAgICAgICAgICAgICAgICAidmFsdWVUeXBlIiA6ICJ7dXJuOmhsNy1vcmc6ZWxtLXR5cGVzOnIxfUludGVnZXIiLA0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6ICIxIiwNCiAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJMaXRlcmFsIg0KICAgICAgICAgICAgICAgICAgfSwNCiAgICAgICAgICAgICAgICAgICJob3VyIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZVR5cGUiIDogInt1cm46aGw3LW9yZzplbG0tdHlwZXM6cjF9SW50ZWdlciIsDQogICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogIjAiLA0KICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkxpdGVyYWwiDQogICAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgICAgIm1pbnV0ZSIgOiB7DQogICAgICAgICAgICAgICAgICAgICAidmFsdWVUeXBlIiA6ICJ7dXJuOmhsNy1vcmc6ZWxtLXR5cGVzOnIxfUludGVnZXIiLA0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6ICIwIiwNCiAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJMaXRlcmFsIg0KICAgICAgICAgICAgICAgICAgfSwNCiAgICAgICAgICAgICAgICAgICJzZWNvbmQiIDogew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlVHlwZSIgOiAie3VybjpobDctb3JnOmVsbS10eXBlczpyMX1JbnRlZ2VyIiwNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiAiMCIsDQogICAgICAgICAgICAgICAgICAgICAidHlwZSIgOiAiTGl0ZXJhbCINCiAgICAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAgICAibWlsbGlzZWNvbmQiIDogew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlVHlwZSIgOiAie3VybjpobDctb3JnOmVsbS10eXBlczpyMX1JbnRlZ2VyIiwNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiAiMCIsDQogICAgICAgICAgICAgICAgICAgICAidHlwZSIgOiAiTGl0ZXJhbCINCiAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAiaGlnaCIgOiB7DQogICAgICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiOSIsDQogICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMjQ6NDQtMjQ6NjUiLA0KICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkRhdGVUaW1lIiwNCiAgICAgICAgICAgICAgICAgICJ5ZWFyIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZVR5cGUiIDogInt1cm46aGw3LW9yZzplbG0tdHlwZXM6cjF9SW50ZWdlciIsDQogICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogIjIwMjAiLA0KICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkxpdGVyYWwiDQogICAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgICAgIm1vbnRoIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZVR5cGUiIDogInt1cm46aGw3LW9yZzplbG0tdHlwZXM6cjF9SW50ZWdlciIsDQogICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogIjEiLA0KICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkxpdGVyYWwiDQogICAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgICAgImRheSIgOiB7DQogICAgICAgICAgICAgICAgICAgICAidmFsdWVUeXBlIiA6ICJ7dXJuOmhsNy1vcmc6ZWxtLXR5cGVzOnIxfUludGVnZXIiLA0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6ICIxIiwNCiAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJMaXRlcmFsIg0KICAgICAgICAgICAgICAgICAgfSwNCiAgICAgICAgICAgICAgICAgICJob3VyIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZVR5cGUiIDogInt1cm46aGw3LW9yZzplbG0tdHlwZXM6cjF9SW50ZWdlciIsDQogICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogIjAiLA0KICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkxpdGVyYWwiDQogICAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgICAgIm1pbnV0ZSIgOiB7DQogICAgICAgICAgICAgICAgICAgICAidmFsdWVUeXBlIiA6ICJ7dXJuOmhsNy1vcmc6ZWxtLXR5cGVzOnIxfUludGVnZXIiLA0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6ICIwIiwNCiAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJMaXRlcmFsIg0KICAgICAgICAgICAgICAgICAgfSwNCiAgICAgICAgICAgICAgICAgICJzZWNvbmQiIDogew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlVHlwZSIgOiAie3VybjpobDctb3JnOmVsbS10eXBlczpyMX1JbnRlZ2VyIiwNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiAiMCIsDQogICAgICAgICAgICAgICAgICAgICAidHlwZSIgOiAiTGl0ZXJhbCINCiAgICAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAgICAibWlsbGlzZWNvbmQiIDogew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlVHlwZSIgOiAie3VybjpobDctb3JnOmVsbS10eXBlczpyMX1JbnRlZ2VyIiwNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiAiMCIsDQogICAgICAgICAgICAgICAgICAgICAidHlwZSIgOiAiTGl0ZXJhbCINCiAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgIH0sDQogICAgICAgICAgICAicGFyYW1ldGVyVHlwZVNwZWNpZmllciIgOiB7DQogICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiMTIiLA0KICAgICAgICAgICAgICAgImxvY2F0b3IiIDogIjIzOjMyLTIzOjQ5IiwNCiAgICAgICAgICAgICAgICJ0eXBlIiA6ICJJbnRlcnZhbFR5cGVTcGVjaWZpZXIiLA0KICAgICAgICAgICAgICAgInBvaW50VHlwZSIgOiB7DQogICAgICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiMTEiLA0KICAgICAgICAgICAgICAgICAgImxvY2F0b3IiIDogIjIzOjQxLTIzOjQ4IiwNCiAgICAgICAgICAgICAgICAgICJuYW1lIiA6ICJ7dXJuOmhsNy1vcmc6ZWxtLXR5cGVzOnIxfURhdGVUaW1lIiwNCiAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJOYW1lZFR5cGVTcGVjaWZpZXIiDQogICAgICAgICAgICAgICB9DQogICAgICAgICAgICB9DQogICAgICAgICB9IF0NCiAgICAgIH0sDQogICAgICAidmFsdWVTZXRzIiA6IHsNCiAgICAgICAgICJkZWYiIDogWyB7DQogICAgICAgICAgICAibG9jYWxJZCIgOiAiMyIsDQogICAgICAgICAgICAibG9jYXRvciIgOiAiMTc6MS0xNzoxMDQiLA0KICAgICAgICAgICAgIm5hbWUiIDogIk9mZmljZSBWaXNpdCIsDQogICAgICAgICAgICAiaWQiIDogImh0dHA6Ly9jdHMubmxtLm5paC5nb3YvZmhpci9WYWx1ZVNldC8yLjE2Ljg0MC4xLjExMzg4My4zLjQ2NC4xMDAzLjEwMS4xMi4xMDAxIiwNCiAgICAgICAgICAgICJhY2Nlc3NMZXZlbCIgOiAiUHVibGljIiwNCiAgICAgICAgICAgICJhbm5vdGF0aW9uIiA6IFsgew0KICAgICAgICAgICAgICAgInR5cGUiIDogIkFubm90YXRpb24iLA0KICAgICAgICAgICAgICAgInMiIDogew0KICAgICAgICAgICAgICAgICAgInIiIDogIjMiLA0KICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiIiwidmFsdWVzZXQgIiwiXCJPZmZpY2UgVmlzaXRcIiIsIjogIiwiJ2h0dHA6Ly9jdHMubmxtLm5paC5nb3YvZmhpci9WYWx1ZVNldC8yLjE2Ljg0MC4xLjExMzg4My4zLjQ2NC4xMDAzLjEwMS4xMi4xMDAxJyIgXQ0KICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICB9DQogICAgICAgICAgICB9IF0NCiAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICJsb2NhbElkIiA6ICI0IiwNCiAgICAgICAgICAgICJsb2NhdG9yIiA6ICIxODoxLTE4OjEwMyIsDQogICAgICAgICAgICAibmFtZSIgOiAiQW5udWFsIFdlbGxuZXNzIFZpc2l0IiwNCiAgICAgICAgICAgICJpZCIgOiAiaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNTI2LjMuMTI0MCIsDQogICAgICAgICAgICAiYWNjZXNzTGV2ZWwiIDogIlB1YmxpYyIsDQogICAgICAgICAgICAiYW5ub3RhdGlvbiIgOiBbIHsNCiAgICAgICAgICAgICAgICJ0eXBlIiA6ICJBbm5vdGF0aW9uIiwNCiAgICAgICAgICAgICAgICJzIiA6IHsNCiAgICAgICAgICAgICAgICAgICJyIiA6ICI0IiwNCiAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIiIsInZhbHVlc2V0ICIsIlwiQW5udWFsIFdlbGxuZXNzIFZpc2l0XCIiLCI6ICIsIidodHRwOi8vY3RzLm5sbS5uaWguZ292L2ZoaXIvVmFsdWVTZXQvMi4xNi44NDAuMS4xMTM4ODMuMy41MjYuMy4xMjQwJyIgXQ0KICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICB9DQogICAgICAgICAgICB9IF0NCiAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICJsb2NhbElkIiA6ICI1IiwNCiAgICAgICAgICAgICJsb2NhdG9yIiA6ICIxOToxLTE5OjE1NCIsDQogICAgICAgICAgICAibmFtZSIgOiAiUHJldmVudGl2ZSBDYXJlIFNlcnZpY2VzIC0gRXN0YWJsaXNoZWQgT2ZmaWNlIFZpc2l0LCAxOCBhbmQgVXAiLA0KICAgICAgICAgICAgImlkIiA6ICJodHRwOi8vY3RzLm5sbS5uaWguZ292L2ZoaXIvVmFsdWVTZXQvMi4xNi44NDAuMS4xMTM4ODMuMy40NjQuMTAwMy4xMDEuMTIuMTAyNSIsDQogICAgICAgICAgICAiYWNjZXNzTGV2ZWwiIDogIlB1YmxpYyIsDQogICAgICAgICAgICAiYW5ub3RhdGlvbiIgOiBbIHsNCiAgICAgICAgICAgICAgICJ0eXBlIiA6ICJBbm5vdGF0aW9uIiwNCiAgICAgICAgICAgICAgICJzIiA6IHsNCiAgICAgICAgICAgICAgICAgICJyIiA6ICI1IiwNCiAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIiIsInZhbHVlc2V0ICIsIlwiUHJldmVudGl2ZSBDYXJlIFNlcnZpY2VzIC0gRXN0YWJsaXNoZWQgT2ZmaWNlIFZpc2l0LCAxOCBhbmQgVXBcIiIsIjogIiwiJ2h0dHA6Ly9jdHMubmxtLm5paC5nb3YvZmhpci9WYWx1ZVNldC8yLjE2Ljg0MC4xLjExMzg4My4zLjQ2NC4xMDAzLjEwMS4xMi4xMDI1JyIgXQ0KICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICB9DQogICAgICAgICAgICB9IF0NCiAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICJsb2NhbElkIiA6ICI2IiwNCiAgICAgICAgICAgICJsb2NhdG9yIiA6ICIyMDoxLTIwOjE0OCIsDQogICAgICAgICAgICAibmFtZSIgOiAiUHJldmVudGl2ZSBDYXJlIFNlcnZpY2VzLUluaXRpYWwgT2ZmaWNlIFZpc2l0LCAxOCBhbmQgVXAiLA0KICAgICAgICAgICAgImlkIiA6ICJodHRwOi8vY3RzLm5sbS5uaWguZ292L2ZoaXIvVmFsdWVTZXQvMi4xNi44NDAuMS4xMTM4ODMuMy40NjQuMTAwMy4xMDEuMTIuMTAyMyIsDQogICAgICAgICAgICAiYWNjZXNzTGV2ZWwiIDogIlB1YmxpYyIsDQogICAgICAgICAgICAiYW5ub3RhdGlvbiIgOiBbIHsNCiAgICAgICAgICAgICAgICJ0eXBlIiA6ICJBbm5vdGF0aW9uIiwNCiAgICAgICAgICAgICAgICJzIiA6IHsNCiAgICAgICAgICAgICAgICAgICJyIiA6ICI2IiwNCiAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIiIsInZhbHVlc2V0ICIsIlwiUHJldmVudGl2ZSBDYXJlIFNlcnZpY2VzLUluaXRpYWwgT2ZmaWNlIFZpc2l0LCAxOCBhbmQgVXBcIiIsIjogIiwiJ2h0dHA6Ly9jdHMubmxtLm5paC5nb3YvZmhpci9WYWx1ZVNldC8yLjE2Ljg0MC4xLjExMzg4My4zLjQ2NC4xMDAzLjEwMS4xMi4xMDIzJyIgXQ0KICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICB9DQogICAgICAgICAgICB9IF0NCiAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICJsb2NhbElkIiA6ICI3IiwNCiAgICAgICAgICAgICJsb2NhdG9yIiA6ICIyMToxLTIxOjExNiIsDQogICAgICAgICAgICAibmFtZSIgOiAiSG9tZSBIZWFsdGhjYXJlIFNlcnZpY2VzIiwNCiAgICAgICAgICAgICJpZCIgOiAiaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNDY0LjEwMDMuMTAxLjEyLjEwMTYiLA0KICAgICAgICAgICAgImFjY2Vzc0xldmVsIiA6ICJQdWJsaWMiLA0KICAgICAgICAgICAgImFubm90YXRpb24iIDogWyB7DQogICAgICAgICAgICAgICAidHlwZSIgOiAiQW5ub3RhdGlvbiIsDQogICAgICAgICAgICAgICAicyIgOiB7DQogICAgICAgICAgICAgICAgICAiciIgOiAiNyIsDQogICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICIiLCJ2YWx1ZXNldCAiLCJcIkhvbWUgSGVhbHRoY2FyZSBTZXJ2aWNlc1wiIiwiOiAiLCInaHR0cDovL2N0cy5ubG0ubmloLmdvdi9maGlyL1ZhbHVlU2V0LzIuMTYuODQwLjEuMTEzODgzLjMuNDY0LjEwMDMuMTAxLjEyLjEwMTYnIiBdDQogICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgIH0gXQ0KICAgICAgICAgfSBdDQogICAgICB9LA0KICAgICAgImNvbnRleHRzIiA6IHsNCiAgICAgICAgICJkZWYiIDogWyB7DQogICAgICAgICAgICAibG9jYXRvciIgOiAiMjY6MS0yNjoxNSIsDQogICAgICAgICAgICAibmFtZSIgOiAiUGF0aWVudCINCiAgICAgICAgIH0gXQ0KICAgICAgfSwNCiAgICAgICJzdGF0ZW1lbnRzIiA6IHsNCiAgICAgICAgICJkZWYiIDogWyB7DQogICAgICAgICAgICAibG9jYXRvciIgOiAiMjY6MS0yNjoxNSIsDQogICAgICAgICAgICAibmFtZSIgOiAiUGF0aWVudCIsDQogICAgICAgICAgICAiY29udGV4dCIgOiAiUGF0aWVudCIsDQogICAgICAgICAgICAiZXhwcmVzc2lvbiIgOiB7DQogICAgICAgICAgICAgICAidHlwZSIgOiAiU2luZ2xldG9uRnJvbSIsDQogICAgICAgICAgICAgICAib3BlcmFuZCIgOiB7DQogICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMjY6MS0yNjoxNSIsDQogICAgICAgICAgICAgICAgICAiZGF0YVR5cGUiIDogIntodHRwOi8vaGw3Lm9yZy9maGlyfVBhdGllbnQiLA0KICAgICAgICAgICAgICAgICAgInRlbXBsYXRlSWQiIDogImh0dHA6Ly9obDcub3JnL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9QYXRpZW50IiwNCiAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJSZXRyaWV2ZSINCiAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgIH0NCiAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICJsb2NhbElkIiA6ICIzNCIsDQogICAgICAgICAgICAibG9jYXRvciIgOiAiMjg6MS0zNzo0MyIsDQogICAgICAgICAgICAibmFtZSIgOiAiUXVhbGlmeWluZyBFbmNvdW50ZXJzIiwNCiAgICAgICAgICAgICJjb250ZXh0IiA6ICJQYXRpZW50IiwNCiAgICAgICAgICAgICJhY2Nlc3NMZXZlbCIgOiAiUHVibGljIiwNCiAgICAgICAgICAgICJhbm5vdGF0aW9uIiA6IFsgew0KICAgICAgICAgICAgICAgInR5cGUiIDogIkFubm90YXRpb24iLA0KICAgICAgICAgICAgICAgInMiIDogew0KICAgICAgICAgICAgICAgICAgInIiIDogIjM0IiwNCiAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIiIsImRlZmluZSAiLCJcIlF1YWxpZnlpbmcgRW5jb3VudGVyc1wiIiwiOlxuXHQiIF0NCiAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICJyIiA6ICIzMyIsDQogICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjIzIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjIyIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIihcbiAgICAiIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJyIiA6ICIyMiIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJyIiA6ICIyMCIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJyIiA6ICIxOCIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJyIiA6ICIxNiIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJyIiA6ICIxNCIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJbIiwiRW5jb3VudGVyIiwiOiAiIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIlwiT2ZmaWNlIFZpc2l0XCIiIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIl0iIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIlxuICBcdFx0dW5pb24gIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiciIgOiAiMTUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiWyIsIkVuY291bnRlciIsIjogIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJcIkFubnVhbCBXZWxsbmVzcyBWaXNpdFwiIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJdIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIlxuICBcdFx0dW5pb24gIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiciIgOiAiMTciLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiWyIsIkVuY291bnRlciIsIjogIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJcIlByZXZlbnRpdmUgQ2FyZSBTZXJ2aWNlcyAtIEVzdGFibGlzaGVkIE9mZmljZSBWaXNpdCwgMTggYW5kIFVwXCIiIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIl0iIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiXG4gIFx0XHR1bmlvbiAiIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJyIiA6ICIxOSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJbIiwiRW5jb3VudGVyIiwiOiAiIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIlwiUHJldmVudGl2ZSBDYXJlIFNlcnZpY2VzLUluaXRpYWwgT2ZmaWNlIFZpc2l0LCAxOCBhbmQgVXBcIiIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiXSIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJcbiAgXHRcdHVuaW9uICIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjIxIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIlsiLCJFbmNvdW50ZXIiLCI6ICIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiXCJIb21lIEhlYWx0aGNhcmUgU2VydmljZXNcIiIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiXSIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJcbiAgKSIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiICIsIlZhbGlkRW5jb3VudGVyIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIlxuXHRcdCIgXQ0KICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjMyIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIndoZXJlICIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjMyIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjI3IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjI1IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjI0IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIlZhbGlkRW5jb3VudGVyIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICIuIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiciIgOiAiMjUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAicGVyaW9kIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjI3IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICIgIiwiZHVyaW5nIiwiICIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjI2IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIlwiTWVhc3VyZW1lbnQgUGVyaW9kXCIiIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiXG4gIFx0XHRhbmQgIiBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiciIgOiAiMzEiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiciIgOiAiMjkiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiciIgOiAiMjgiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInMiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiVmFsaWRFbmNvdW50ZXIiIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIi4iIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJyIiA6ICIyOSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAicyIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiBbICJzdGF0dXMiIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidmFsdWUiIDogWyAiICAiLCI9IiwiICIgXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInIiIDogIjMwIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInZhbHVlIiA6IFsgIidmaW5pc2hlZCciIF0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgIH0gXSwNCiAgICAgICAgICAgICJleHByZXNzaW9uIiA6IHsNCiAgICAgICAgICAgICAgICJsb2NhbElkIiA6ICIzMyIsDQogICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMjk6Mi0zNzo0MyIsDQogICAgICAgICAgICAgICAidHlwZSIgOiAiUXVlcnkiLA0KICAgICAgICAgICAgICAgInNvdXJjZSIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICJsb2NhbElkIiA6ICIyMyIsDQogICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMjk6Mi0zNToxOCIsDQogICAgICAgICAgICAgICAgICAiYWxpYXMiIDogIlZhbGlkRW5jb3VudGVyIiwNCiAgICAgICAgICAgICAgICAgICJleHByZXNzaW9uIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICJsb2NhbElkIiA6ICIyMiIsDQogICAgICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMjk6Mi0zNTozIiwNCiAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJVbmlvbiIsDQogICAgICAgICAgICAgICAgICAgICAib3BlcmFuZCIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICJsb2NhbElkIiA6ICIyMCIsDQogICAgICAgICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMzA6NS0zMzo4MSIsDQogICAgICAgICAgICAgICAgICAgICAgICAidHlwZSIgOiAiVW5pb24iLA0KICAgICAgICAgICAgICAgICAgICAgICAgIm9wZXJhbmQiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiMTYiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgImxvY2F0b3IiIDogIjMwOjUtMzE6NDYiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIlVuaW9uIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJvcGVyYW5kIiA6IFsgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImxvY2FsSWQiIDogIjE0IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJsb2NhdG9yIiA6ICIzMDo1LTMwOjMxIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkYXRhVHlwZSIgOiAie2h0dHA6Ly9obDcub3JnL2ZoaXJ9RW5jb3VudGVyIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0ZW1wbGF0ZUlkIiA6ICJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vRW5jb3VudGVyIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlUHJvcGVydHkiIDogInR5cGUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGVDb21wYXJhdG9yIiA6ICJpbiIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidHlwZSIgOiAiUmV0cmlldmUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGVzIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJsb2NhdG9yIiA6ICIzMDoxNy0zMDozMCIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAibmFtZSIgOiAiT2ZmaWNlIFZpc2l0IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJWYWx1ZVNldFJlZiINCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJsb2NhbElkIiA6ICIxNSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMzE6MTEtMzE6NDYiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImRhdGFUeXBlIiA6ICJ7aHR0cDovL2hsNy5vcmcvZmhpcn1FbmNvdW50ZXIiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInRlbXBsYXRlSWQiIDogImh0dHA6Ly9obDcub3JnL2ZoaXIvU3RydWN0dXJlRGVmaW5pdGlvbi9FbmNvdW50ZXIiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGVQcm9wZXJ0eSIgOiAidHlwZSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZUNvbXBhcmF0b3IiIDogImluIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJSZXRyaWV2ZSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZXMiIDogew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImxvY2F0b3IiIDogIjMxOjIzLTMxOjQ1IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJuYW1lIiA6ICJBbm51YWwgV2VsbG5lc3MgVmlzaXQiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIlZhbHVlU2V0UmVmIg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAidHlwZSIgOiAiVW5pb24iLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIm9wZXJhbmQiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiMTciLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImxvY2F0b3IiIDogIjMyOjExLTMyOjg3IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJkYXRhVHlwZSIgOiAie2h0dHA6Ly9obDcub3JnL2ZoaXJ9RW5jb3VudGVyIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0ZW1wbGF0ZUlkIiA6ICJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vRW5jb3VudGVyIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlUHJvcGVydHkiIDogInR5cGUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGVDb21wYXJhdG9yIiA6ICJpbiIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidHlwZSIgOiAiUmV0cmlldmUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImNvZGVzIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJsb2NhdG9yIiA6ICIzMjoyMy0zMjo4NiIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAibmFtZSIgOiAiUHJldmVudGl2ZSBDYXJlIFNlcnZpY2VzIC0gRXN0YWJsaXNoZWQgT2ZmaWNlIFZpc2l0LCAxOCBhbmQgVXAiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIlZhbHVlU2V0UmVmIg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgfQ0KICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImxvY2FsSWQiIDogIjE5IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJsb2NhdG9yIiA6ICIzMzoxMS0zMzo4MSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiZGF0YVR5cGUiIDogIntodHRwOi8vaGw3Lm9yZy9maGlyfUVuY291bnRlciIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidGVtcGxhdGVJZCIgOiAiaHR0cDovL2hsNy5vcmcvZmhpci9TdHJ1Y3R1cmVEZWZpbml0aW9uL0VuY291bnRlciIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiY29kZVByb3BlcnR5IiA6ICJ0eXBlIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlQ29tcGFyYXRvciIgOiAiaW4iLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIlJldHJpZXZlIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlcyIgOiB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMzM6MjMtMzM6ODAiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIm5hbWUiIDogIlByZXZlbnRpdmUgQ2FyZSBTZXJ2aWNlcy1Jbml0aWFsIE9mZmljZSBWaXNpdCwgMTggYW5kIFVwIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJWYWx1ZVNldFJlZiINCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiMjEiLA0KICAgICAgICAgICAgICAgICAgICAgICAgImxvY2F0b3IiIDogIjM0OjExLTM0OjQ5IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJkYXRhVHlwZSIgOiAie2h0dHA6Ly9obDcub3JnL2ZoaXJ9RW5jb3VudGVyIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJ0ZW1wbGF0ZUlkIiA6ICJodHRwOi8vaGw3Lm9yZy9maGlyL1N0cnVjdHVyZURlZmluaXRpb24vRW5jb3VudGVyIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJjb2RlUHJvcGVydHkiIDogInR5cGUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgImNvZGVDb21wYXJhdG9yIiA6ICJpbiIsDQogICAgICAgICAgICAgICAgICAgICAgICAidHlwZSIgOiAiUmV0cmlldmUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgImNvZGVzIiA6IHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJsb2NhdG9yIiA6ICIzNDoyMy0zNDo0OCIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAibmFtZSIgOiAiSG9tZSBIZWFsdGhjYXJlIFNlcnZpY2VzIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJWYWx1ZVNldFJlZiINCiAgICAgICAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgfQ0KICAgICAgICAgICAgICAgfSBdLA0KICAgICAgICAgICAgICAgInJlbGF0aW9uc2hpcCIgOiBbIF0sDQogICAgICAgICAgICAgICAid2hlcmUiIDogew0KICAgICAgICAgICAgICAgICAgImxvY2FsSWQiIDogIjMyIiwNCiAgICAgICAgICAgICAgICAgICJsb2NhdG9yIiA6ICIzNjozLTM3OjQzIiwNCiAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJBbmQiLA0KICAgICAgICAgICAgICAgICAgIm9wZXJhbmQiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiMjciLA0KICAgICAgICAgICAgICAgICAgICAgImxvY2F0b3IiIDogIjM2OjktMzY6NTciLA0KICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkluY2x1ZGVkSW4iLA0KICAgICAgICAgICAgICAgICAgICAgIm9wZXJhbmQiIDogWyB7DQogICAgICAgICAgICAgICAgICAgICAgICAibmFtZSIgOiAiVG9JbnRlcnZhbCIsDQogICAgICAgICAgICAgICAgICAgICAgICAibGlicmFyeU5hbWUiIDogIkZISVJIZWxwZXJzIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJGdW5jdGlvblJlZiIsDQogICAgICAgICAgICAgICAgICAgICAgICAib3BlcmFuZCIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJsb2NhbElkIiA6ICIyNSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMzY6OS0zNjoyOSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAicGF0aCIgOiAicGVyaW9kIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJzY29wZSIgOiAiVmFsaWRFbmNvdW50ZXIiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIlByb3BlcnR5Ig0KICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiMjYiLA0KICAgICAgICAgICAgICAgICAgICAgICAgImxvY2F0b3IiIDogIjM2OjM4LTM2OjU3IiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJuYW1lIiA6ICJNZWFzdXJlbWVudCBQZXJpb2QiLA0KICAgICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIlBhcmFtZXRlclJlZiINCiAgICAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgICAgfSwgew0KICAgICAgICAgICAgICAgICAgICAgImxvY2FsSWQiIDogIjMxIiwNCiAgICAgICAgICAgICAgICAgICAgICJsb2NhdG9yIiA6ICIzNzo5LTM3OjQzIiwNCiAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJFcXVhbCIsDQogICAgICAgICAgICAgICAgICAgICAib3BlcmFuZCIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICJuYW1lIiA6ICJUb1N0cmluZyIsDQogICAgICAgICAgICAgICAgICAgICAgICAibGlicmFyeU5hbWUiIDogIkZISVJIZWxwZXJzIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJ0eXBlIiA6ICJGdW5jdGlvblJlZiIsDQogICAgICAgICAgICAgICAgICAgICAgICAib3BlcmFuZCIgOiBbIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJsb2NhbElkIiA6ICIyOSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAibG9jYXRvciIgOiAiMzc6OS0zNzoyOSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAicGF0aCIgOiAic3RhdHVzIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICJzY29wZSIgOiAiVmFsaWRFbmNvdW50ZXIiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIlByb3BlcnR5Ig0KICAgICAgICAgICAgICAgICAgICAgICAgfSBdDQogICAgICAgICAgICAgICAgICAgICB9LCB7DQogICAgICAgICAgICAgICAgICAgICAgICAibG9jYWxJZCIgOiAiMzAiLA0KICAgICAgICAgICAgICAgICAgICAgICAgImxvY2F0b3IiIDogIjM3OjM0LTM3OjQzIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZVR5cGUiIDogInt1cm46aGw3LW9yZzplbG0tdHlwZXM6cjF9U3RyaW5nIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICJ2YWx1ZSIgOiAiZmluaXNoZWQiLA0KICAgICAgICAgICAgICAgICAgICAgICAgInR5cGUiIDogIkxpdGVyYWwiDQogICAgICAgICAgICAgICAgICAgICB9IF0NCiAgICAgICAgICAgICAgICAgIH0gXQ0KICAgICAgICAgICAgICAgfQ0KICAgICAgICAgICAgfQ0KICAgICAgICAgfSBdDQogICAgICB9DQogICB9DQp9\"\n" +
            "  } ]\n" +
            "}";

    private final String SIMPLE_IG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!-- Start by finding all references to \"myig\" and updating to appropriate text for your IG, including changing realm -->\n" +
            "<ImplementationGuide xmlns=\"http://hl7.org/fhir\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://hl7.org/fhir ../input-cache/schemas/R4/fhir-single.xsd\">\n" +
            "  <id value=\"example.fhir.uv.myig\"/>\n" +
            "  <extension url=\"http://hl7.org/fhir/StructureDefinition/structuredefinition-standards-status\">\n" +
            "    <valueCode value=\"informative\"/> \n" +
            "  </extension> \n" +
            "  <extension url=\"http://hl7.org/fhir/StructureDefinition/structuredefinition-fmm\">\n" +
            "    <valueInteger value=\"1\"/> \n" +
            "  </extension>\n" +
            "<!--  <extension url=\"http://hl7.org/fhir/tools/StructureDefinition/igpublisher-spreadsheet\">\n" +
            "    <valueString value=\"resources-spreadsheet.xml\"/>\n" +
            "  </extension>-->\n" +
            "  <url value=\"http://somewhere.org/fhir/uv/myig/ImplementationGuide/example.fhir.uv.myig\"/>\n" +
            "  <!-- This version will propagate to all artifacts unless the \"propagate-version\" extension is overridden -->\n" +
            "  <version value=\"0.2.0\"/>\n" +
            "  <name value=\"YourComputableMyIGNameHere\"/>\n" +
            "  <title value=\"Your User Friendly Name for MyIG Here\"/>\n" +
            "  <status value=\"draft\"/>\n" +
            "  <experimental value=\"false\"/>\n" +
            "  <publisher value=\"HL7 International - [Some] Work Group\"/>\n" +
            "  <contact>\n" +
            "    <telecom>\n" +
            "      <!-- Or whatever URL and/or email address(es) are appropriate -->\n" +
            "      <system value=\"url\"/>\n" +
            "      <value value=\"http://hl7.org/Special/committees/[something]\"/>\n" +
            "    </telecom>\n" +
            "  </contact>\n" +
            "  <description value=\"A brief description of what MyIG is about (probably the same text as in your readme)\"/>\n" +
            "  <jurisdiction>\n" +
            "    <!-- This first repetition will drive SNOMED release used -->\n" +
            "    <coding>\n" +
            "      <system value=\"http://unstats.un.org/unsd/methods/m49/m49.htm\"/>\n" +
            "      <!-- This is the code for universal -->\n" +
            "      <code value=\"001\"/>\n" +
            "    </coding>\n" +
            "  </jurisdiction>\n" +
            "  <packageId value=\"example.fhir.uv.myig\"/>\n" +
            "  <!-- This should be changed to 'not-open-source' or another license if appropriate for non-HL7-published content -->\n" +
            "  <license value=\"CC0-1.0\"/>\n" +
            "  <!-- This is whatever FHIR version(s) the IG artifacts are targeting (not the version of this file, which should always be 'current release') -->\n" +
            "  <fhirVersion value=\"4.0.1\"/>\n" +
            "  <dependsOn id=\"cqf\">\n" +
            "    <uri value=\"http://fhir.org/guides/cqf/common/ImplementationGuide/fhir.cqf.common\"/>\n" +
            "    <packageId value=\"fhir.cqf.common\"/>\n" +
            "    <version value=\"4.0.1\"/>\n" +
            "  </dependsOn>\n" +
            "  <dependsOn>\n" +
            "    <uri value=\"http://fhir.org/guides/cdc/opioid-mme-r4/ImplementationGuide/fhir.cdc.opioid-mme-r4\" />\n" +
            "    <packageId value=\"fhir.cdc.opioid-mme-r4\" />\n" +
            "    <version value=\"3.0.0\" />\n" +
            "  </dependsOn>" +
            "<!--  <dependsOn>\n" +
            "    <uri value=\"https://simplifier.net/packages/de.medizininformatikinitiative.kerndatensatz.fall/0.9.1/files/131317\"/>\n" +
            "    <packageId value=\"de.medizininformatikinitiative.kerndatensatz.fall\"/>\n" +
            "    <version value=\"0.9.1\"/>\n" +
            "  </dependsOn>-->\n" +
            "  <definition>\n" +
            "    <!-- You don't need to define any groupings.  The IGPublisher will define them for you.  You only need to do so if your IG is 'special' and it's\n" +
            "      inappropriate to use the defaults.  Feel free to provide feedback about the defaults... -->\n" +
            "    <resource>\n" +
            "      <reference>\n" +
            "        <reference value=\"StructureDefinition/mypatient\"/>\n" +
            "      </reference>\n" +
            "      <description value=\"Overriding description because we can\"/>\n" +
            "    </resource>\n" +
            "    <resource>\n" +
            "      <reference>\n" +
            "        <reference value=\"StructureDefinition/MyLogical\"/>\n" +
            "      </reference>\n" +
            "      <description value=\"Logical Model\"/>\n" +
            "    </resource>\n" +
            "    <resource>\n" +
            "      <reference>\n" +
            "        <reference value=\"Library/example-sql\"/>\n" +
            "      </reference>\n" +
            "      <description value=\"A test example\"/>\n" +
            "    </resource>\n" +
            "    <resource>\n" +
            "      <reference>\n" +
            "        <reference value=\"Library/example-image\"/>\n" +
            "      </reference>\n" +
            "      <description value=\"A test image example\"/>\n" +
            "    </resource>\n" +
            "<!--<resource>\n" +
            "<reference>\n" +
            "<reference value=\"MyLogicalModel/AS\"/>\n" +
            "</reference>\n" +
            "</resource> -->\n" +
            "    <resource>\n" +
            "      <reference>\n" +
            "        <reference value=\"Patient/example\"/>\n" +
            "      </reference>\n" +
            "      <name value=\"Simple patient example\"/>\n" +
            "      <description value=\"A simple example showing how examples are defined and referenced\"/>\n" +
            "      <exampleCanonical value=\"http://somewhere.org/fhir/uv/myig/StructureDefinition/mypatient\"/>\n" +
            "    </resource>\n" +
            "    <resource>\n" +
            "      <reference>\n" +
            "        <reference value=\"Observation/example\"/>\n" +
            "      </reference>\n" +
            "      <name value=\"Observation patient example\"/>\n" +
            "      <description value=\"Example showing slicing\"/>\n" +
            "      <exampleCanonical value=\"http://somewhere.org/fhir/uv/myig/StructureDefinition/myObservation\"/>\n" +
            "    </resource>\n" +
            "    <resource>\n" +
            "      <reference>\n" +
            "        <reference value=\"Binary/example\"/>\n" +
            "      </reference>\n" +
            "      <name value=\"Example binary\"/>\n" +
            "      <description value=\"Example showing binary content\"/>\n" +
            "    </resource>\n" +
            "    <resource>\n" +
            "      <extension url=\"http://hl7.org/fhir/StructureDefinition/implementationguide-resource-format\">\n" +
            "        <valueCode value=\"application/xml\"/>\n" +
            "      </extension>\n" +
            "      <reference>\n" +
            "        <reference value=\"Binary/logical-example\"/>\n" +
            "      </reference>\n" +
            "      <name value=\"Example of Logical Model\"/>\n" +
            "      <description value=\"Example showing example content for a logical model\"/>\n" +
            "      <exampleCanonical value=\"http://somewhere.org/fhir/uv/myig/StructureDefinition/MyLogical\"/>\n" +
            "    </resource>    \n" +
            "    <resource>\n" +
            "      <extension url=\"http://hl7.org/fhir/StructureDefinition/implementationguide-resource-format\">\n" +
            "        <valueCode value=\"image/jpeg\"/>\n" +
            "      </extension>\n" +
            "      <reference>\n" +
            "        <reference value=\"Binary/image-example\"/>\n" +
            "      </reference>\n" +
            "      <name value=\"Example of Binary Image\"/>\n" +
            "      <description value=\"Example showing example content for an image\"/>\n" +
            "    </resource>    \n" +
            "    <resource>\n" +
            "      <reference>\n" +
            "        <reference value=\"ValueSet/valueset-no-codesystem\"/>\n" +
            "      </reference>\n" +
            "      <name value=\"Value set for no code system\"/>\n" +
            "    </resource>\n" +
            "<!--    <resource>\n" +
            "      <reference>\n" +
            "        <reference value=\"Library/example\"/>\n" +
            "      </reference>\n" +
            "      <name value=\"Simple library example\"/>\n" +
            "      <description value=\"A simple example showing how library is pre-loaded\"/>\n" +
            "    </resource> -->\n" +
            "    <page>\n" +
            "      <!-- The root will always be toc.html - the template will force it if you don't do it -->\n" +
            "      <nameUrl value=\"toc.html\"/>\n" +
            "      <title value=\"Table of Contents\"/>\n" +
            "      <generation value=\"html\"/>\n" +
            "      <page>\n" +
            "        <nameUrl value=\"index.html\"/>\n" +
            "        <title value=\"MyIG Home Page\"/>\n" +
            "        <generation value=\"html\"/>\n" +
            "      </page>\n" +
            "      <page>\n" +
            "        <nameUrl value=\"background.html\"/>\n" +
            "        <title value=\"Background\"/>\n" +
            "        <generation value=\"html\"/>\n" +
            "      </page>\n" +
            "      <page>\n" +
            "        <extension url=\"http://hl7.org/fhir/StructureDefinition/structuredefinition-standards-status\">\n" +
            "          <valueCode value=\"trial-use\"/> \n" +
            "        </extension> \n" +
            "        <extension url=\"http://hl7.org/fhir/StructureDefinition/structuredefinition-fmm\">\n" +
            "          <valueInteger value=\"3\"/> \n" +
            "        </extension>\n" +
            "        <nameUrl value=\"spec.html\"/>\n" +
            "        <title value=\"Detailed Specification\"/>\n" +
            "        <generation value=\"markdown\"/>\n" +
            "        <page>\n" +
            "          <nameUrl value=\"spec2.html\"/>\n" +
            "          <title value=\"Spec sub-page\"/>\n" +
            "          <generation value=\"markdown\"/>\n" +
            "        </page>\n" +
            "      </page>\n" +
            "      <page>\n" +
            "        <nameUrl value=\"downloads.html\"/>\n" +
            "        <title value=\"Useful Downloads\"/>\n" +
            "        <generation value=\"html\"/>\n" +
            "      </page>\n" +
            "      <page>\n" +
            "        <nameUrl value=\"changes.html\"/>\n" +
            "        <title value=\"IG Change History\"/>\n" +
            "        <generation value=\"html\"/>\n" +
            "      </page>\n" +
            "    </page>\n" +
            "    <!-- copyright year is a mandatory parameter -->\n" +
            "    <parameter>\n" +
            "      <code value=\"copyrightyear\"/>\n" +
            "      <value value=\"2019+\"/>\n" +
            "    </parameter>\n" +
            "    <!-- releaselabel should be the ballot status for HL7-published IGs. -->\n" +
            "    <parameter>\n" +
            "      <code value=\"releaselabel\"/>\n" +
            "      <value value=\"CI Build\"/>\n" +
            "    </parameter>\n" +
            "    <parameter>\n" +
            "      <code value=\"find-other-resources\"/>\n" +
            "      <value value=\"true\"/>\n" +
            "    </parameter>\n" +
            "    <parameter>\n" +
            "      <code value=\"path-resource\"/>\n" +
            "      <value value=\"input\\history\"/>\n" +
            "    </parameter>\n" +
            "    <parameter>\n" +
            "      <code value=\"path-binary\"/>\n" +
            "      <value value=\"input\\cql\"/>\n" +
            "    </parameter>\n" +
            "    <parameter>\n" +
            "      <code value=\"path-liquid\"/>\n" +
            "      <value value=\"templates\\liquid\"/>\n" +
            "    </parameter>\n" +
            "    <parameter>\n" +
            "      <code value=\"shownav\"/>\n" +
            "      <value value=\"true\"/>\n" +
            "    </parameter>\n" +
            "<!-- Uncomment one or more of these if you want to limit which syntaxes are supported or want to disable the display of mappings\n" +
            "    <parameter>\n" +
            "      <code value=\"excludexml\"/>\n" +
            "      <value value=\"true\"/>\n" +
            "    </parameter>\n" +
            "    <parameter>\n" +
            "      <code value=\"excludejson\"/>\n" +
            "      <value value=\"true\"/>\n" +
            "    </parameter>\n" +
            "    <parameter>\n" +
            "      <code value=\"excludettl\"/>\n" +
            "      <value value=\"true\"/>\n" +
            "    </parameter>\n" +
            "    <parameter>\n" +
            "      <code value=\"excludemap\"/>\n" +
            "      <value value=\"true\"/>\n" +
            "    </parameter>-->\n" +
            "    <parameter>\n" +
            "      <code value=\"showsource\"/>\n" +
            "      <value value=\"true\"/>\n" +
            "    </parameter>\n" +
            "  </definition>\n" +
            "</ImplementationGuide>";
}
