package com.peregrine.functions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;

/**
 * Holds the resolved per-tenant function settings contributed by
 * {@link TenantFunctionsConfig} factory instances and answers the servlet's
 * lookups. A tenant with no configuration gets the empty settings: no fetch
 * targets, no config entries - functions still run, they just cannot reach
 * out.
 */
@Component(service = FunctionTenantRegistry.class)
public class FunctionTenantRegistry {

    private static final FunctionRunner.TenantSettings EMPTY = new FunctionRunner.TenantSettings() {
        @Override public boolean allowsHost(String host) { return false; }
        @Override public Map<String, String> entries() { return Collections.emptyMap(); }
        @Override public List<String> hosts() { return Collections.emptyList(); }
    };

    static final class Instance {
        final List<String> hosts;
        final Map<String, String> entries;

        Instance(List<String> hosts, Map<String, String> entries) {
            this.hosts = hosts;
            this.entries = entries;
        }
    }

    private final Map<String, Instance> byTenant = new ConcurrentHashMap<>();

    void put(String tenant, Instance instance) {
        byTenant.put(tenant, instance);
    }

    void remove(String tenant) {
        byTenant.remove(tenant);
    }

    public FunctionRunner.TenantSettings forTenantAndFunction(String tenant, String function) {
        final Instance instance = byTenant.get(tenant);
        if (instance == null) {
            return EMPTY;
        }
        // entries: plain keys apply to every function; `name/key` overrides
        // `key` for that one function (`/` keeps dotted keys unambiguous)
        final Map<String, String> merged = new LinkedHashMap<>();
        final String prefix = function + "/";
        instance.entries.forEach((k, v) -> {
            if (!k.contains("/")) {
                merged.put(k, v);
            }
        });
        instance.entries.forEach((k, v) -> {
            if (k.startsWith(prefix)) {
                merged.put(k.substring(prefix.length()), v);
            }
        });
        final List<String> allowed = instance.hosts;
        return new FunctionRunner.TenantSettings() {
            @Override
            public boolean allowsHost(String host) {
                if (host == null) {
                    return false;
                }
                final String candidate = host.toLowerCase(Locale.ROOT);
                for (final String entry : allowed) {
                    final String rule = entry.toLowerCase(Locale.ROOT);
                    if (rule.startsWith(".")
                            ? candidate.endsWith(rule) || candidate.equals(rule.substring(1))
                            : candidate.equals(rule)) {
                        return true;
                    }
                }
                return false;
            }

            @Override public Map<String, String> entries() { return merged; }
            @Override public List<String> hosts() { return allowed; }
        };
    }

}
