package com.peregrine.admin.resource;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

@Component(
        service = NodeNameValidation.class,
        immediate = true
)
public class NodeNameValidationService implements NodeNameValidation {

    @Override
    public boolean isValidPageName(String name) {
        return isValidNodeName(name) && !name.contains(".");
    }

    @Override
    public boolean isValidSiteName(String name) {
        return isValidPageName(name) && !name.contains("-");
    }

    @Override
    public boolean isValidNodeName(String name) {
        return StringUtils.isNotBlank(name) &&
                StringUtils.containsNone(name, "/:[]|*?%\"\\\r\n\t") &&
                !name.startsWith(" ") &&
                !name.equals(".") &&
                !name.equals("..");
    }
}
