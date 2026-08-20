package com.peregrine.nodetypes.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.spi.ImplementationPicker;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

/**
 * Routes IComponent adaptations that no resourceType-bound model claimed to
 * {@link PerComponentModel}.
 *
 * The resourceType-based picker (ranking 0) resolves the /apps component node
 * to reach its primary type - which a session that cannot read /apps (the
 * anonymous visitor rendering a public page) can never do. Without this
 * picker such adaptations fall to Sling's FirstImplementationPicker, which
 * returns the alphabetically first IComponent model on the instance - an
 * unrelated theme's model exporting its own fields. Ranked between the two,
 * this picker makes the model-less case deterministic for every session.
 */
@Component(
        service = ImplementationPicker.class,
        property = Constants.SERVICE_RANKING + ":Integer=100"
)
public class PerComponentImplementationPicker implements ImplementationPicker {

    @Override
    public Class<?> pick(Class<?> adapterType, Class<?>[] implementationsTypes, Object adaptable) {
        if (adapterType == IComponent.class && adaptable instanceof Resource) {
            for (final Class<?> implementation : implementationsTypes) {
                if (implementation == PerComponentModel.class) {
                    return implementation;
                }
            }
        }
        return null;
    }

}
