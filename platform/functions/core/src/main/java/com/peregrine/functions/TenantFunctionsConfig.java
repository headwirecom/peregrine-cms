package com.peregrine.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Per-tenant settings for the functions runtime - an OSGi FACTORY
 * configuration, one instance per site. This is where network policy and
 * secrets live: never in function source (which is public-grade content,
 * like a felib), never in the content tree.
 */
@Component(
    configurationPid = "com.peregrine.functions.tenant",
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd = TenantFunctionsConfig.Configuration.class, factory = true)
public class TenantFunctionsConfig {

    @ObjectClassDefinition(
        name = "Peregrine: Functions Tenant Settings",
        description = "Per-site fetch allowlist and configuration entries for server-side functions (factory - one per tenant)"
    )
    public @interface Configuration {
        @AttributeDefinition(name = "Tenant", description = "The site name (/content/<tenant>)")
        String tenant();

        @AttributeDefinition(name = "Fetch allowlist",
            description = "Hosts context.fetch may reach; a leading dot allows the whole suffix (.example.com). Empty = this site's functions cannot fetch.")
        String[] fetchAllowlist() default {};

        @AttributeDefinition(name = "Entries",
            description = "key=value entries exposed as context.config; name/key scopes an entry to one function. Secrets belong here.")
        String[] entries() default {};
    }

    @Reference
    private FunctionTenantRegistry registry;

    private String tenant;

    @Activate
    @Modified
    protected void activate(Configuration configuration) {
        if (tenant != null && !tenant.equals(configuration.tenant())) {
            registry.remove(tenant);
        }
        tenant = configuration.tenant();
        final List<String> hosts = new ArrayList<>();
        for (final String host : configuration.fetchAllowlist()) {
            if (host != null && !host.isBlank()) {
                hosts.add(host.trim());
            }
        }
        final Map<String, String> entries = new LinkedHashMap<>();
        for (final String entry : configuration.entries()) {
            final int eq = entry == null ? -1 : entry.indexOf('=');
            if (eq > 0) {
                entries.put(entry.substring(0, eq).trim(), entry.substring(eq + 1));
            }
        }
        registry.put(tenant, new FunctionTenantRegistry.Instance(
                Collections.unmodifiableList(hosts), Collections.unmodifiableMap(entries)));
    }

    @Deactivate
    protected void deactivate() {
        if (tenant != null) {
            registry.remove(tenant);
        }
    }

}
