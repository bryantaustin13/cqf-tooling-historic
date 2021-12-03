package org.opencds.cqf.tooling.acceleratorkit;

public class StructureDefinitionBindingObject {

    String sdName;
    String sdURL;
    String sdVersion;
    String elementPath;
    String bindingStrength;
    String bindingValueSetURL;
    String bindingValueSetVersion;
    String bindingValueSetName;
    String mustSupport;

    public String getSdName() {return sdName;}
    public void setSdName(String sdName) {this.sdName = sdName;}
    public String getSdURL() {return sdURL;}
    public void setSdURL(String sdURL) {this.sdURL = sdURL;}
    public String getSdVersion() {return sdVersion;}
    public void setSdVersion(String sdVersion) {this.sdVersion = sdVersion;}
    public String getElementPath() {return elementPath;}
    public void setElementPath(String elementPath) {this.elementPath = elementPath;}
    public String getBindingStrength() {return bindingStrength;}
    public void setBindingStrength(String bindingStrength) {this.bindingStrength = bindingStrength;}
    public String getBindingValueSetURL() {return bindingValueSetURL;}
    public void setBindingValueSetURL(String bindingValueSetURL) {this.bindingValueSetURL = bindingValueSetURL;}
    public String getBindingValueSetVersion() {return bindingValueSetVersion;}
    public void setBindingValueSetVersion(String bindingValueSetVersion) {this.bindingValueSetVersion = bindingValueSetVersion;}
    public String getBindingValueSetName() {return bindingValueSetName;}
    public void setBindingValueSetName(String bindingValueSetName) {this.bindingValueSetName = bindingValueSetName;}
    public String getMustSupport() {return mustSupport;}
    public void setMustSupport(String mustSupport) {this.mustSupport = mustSupport;}
}
