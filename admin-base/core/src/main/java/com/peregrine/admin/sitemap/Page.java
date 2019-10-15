package com.peregrine.admin.sitemap;

import com.peregrine.commons.util.PerConstants;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;

public final class Page extends ResourceWrapper {

    private final ValueMap properties;
    private final Resource content;
    private final ValueMap contentProperties;

    public Page(final Resource resource) {
        super(resource);
        properties = resource.getValueMap();
        content = resource.getChild(PerConstants.JCR_CONTENT);
        contentProperties = content == null ? null : content.getValueMap();
    }

    public boolean hasContent() {
        return content != null;
    }

    public boolean containsProperty(final String name) {
        if (contentProperties != null && contentProperties.containsKey(name)) {
            return true;
        }

        return properties.containsKey(name);
    }
}
