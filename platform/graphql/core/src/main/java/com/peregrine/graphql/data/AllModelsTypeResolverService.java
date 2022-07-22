package com.peregrine.graphql.data;

import org.apache.sling.graphql.api.SlingTypeResolver;
import org.apache.sling.graphql.api.SlingTypeResolverEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;

@Component(
    service = SlingTypeResolver.class,
    property = { "name=sites/default"}
)
public class AllModelsTypeResolverService
    implements SlingTypeResolver<Object>
{
    @Override
    public @Nullable Object getType(@NotNull SlingTypeResolverEnvironment slingTypeResolverEnvironment) {
        return null;
    }
}
