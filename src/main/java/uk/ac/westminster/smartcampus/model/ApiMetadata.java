package uk.ac.westminster.smartcampus.model;

import java.util.HashMap;
import java.util.Map;

public class ApiMetadata {
    private String version;
    private String administratorContact;
    
    // the underscore prefix is the industry standard (HAL) for indicating hypermedia links
    private Map<String, String> _links = new HashMap<>();

    public ApiMetadata() {
    }

    public ApiMetadata(String version, String administratorContact) {
        this.version = version;
        this.administratorContact = administratorContact;
    }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getAdministratorContact() { return administratorContact; }
    public void setAdministratorContact(String administratorContact) { this.administratorContact = administratorContact; }

    public Map<String, String> get_links() { return _links; }
    public void set_links(Map<String, String> _links) { this._links = _links; }
    
    // helper method to easily add hypermedia links
    public void addLink(String rel, String href) {
        this._links.put(rel, href);
    }
}