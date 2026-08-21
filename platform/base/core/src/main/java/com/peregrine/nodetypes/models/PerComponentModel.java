package com.peregrine.nodetypes.models;

import static com.peregrine.commons.util.PerConstants.COMPONENT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JSON;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.peregrine.commons.util.PerUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Catch-all model for components that ship no Sling Model of their own.
 *
 * Model resolution for a content node falls through to the /apps component
 * node's jcr:primaryType (per:Component) when neither the resource type nor
 * its super types have a registered model; this class binds there, so a theme
 * can be pure content. Without it, Sling's FirstImplementationPicker would
 * hand the node to the alphabetically first IComponent model on the instance.
 *
 * The export is NOT the raw ValueMap: only properties named by the
 * component's declared structure leave the repository. The declaration is the
 * component folder's model.json (the hatch fragment, shipped by percli),
 * found on the component node or its sling:resourceSuperType chain, with the
 * dialog.json as fallback. Collections ("x-form-type": "collection") export
 * as a named list of the collection node's children, exactly like the
 * generated {@code @Inject List<IComponent>} used to; children export only
 * for "x-type": "container" declarations, mirroring the old
 * Container-vs-AbstractComponent split.
 *
 * Declarations are read through the component-declarations service session
 * (mapped to sling-readall), NOT the request session: /apps is not readable
 * by anonymous, but public pages must still render fully. Parsed
 * declarations are cached per resource type and revalidated against the
 * file's jcr:lastModified at most every few seconds, so steady-state page
 * renders touch no /apps nodes at all.
 */
@Model(adaptables = Resource.class,
       resourceType = {COMPONENT_PRIMARY_TYPE},
       adapters = IComponent.class)
@Exporter(name = JACKSON,
        extensions = JSON)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PerComponentModel extends Container {

    private static final Logger LOG = LoggerFactory.getLogger(PerComponentModel.class);
    private static final String DECLARATIONS_SUB_SERVICE = "component-declarations";
    private static final String MODEL_JSON = "model.json";
    private static final String DIALOG_JSON = "dialog.json";
    private static final String RESOURCE_SUPER_TYPE = "sling:resourceSuperType";
    private static final String JCR_CONTENT_NODE = "jcr:content";
    private static final String JCR_LAST_MODIFIED = "jcr:lastModified";
    private static final String COLLECTION = "collection";
    private static final int MAX_SUPER_TYPE_DEPTH = 10;
    private static final long REVALIDATE_MS = 3_000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ConcurrentHashMap<String, Declaration> CACHE = new ConcurrentHashMap<>();

    private static final class Declaration {
        final Set<String> scalars = new LinkedHashSet<>();
        final Set<String> collections = new LinkedHashSet<>();
        boolean container;
        final long stamp;
        volatile long checkedAt;

        Declaration(long stamp, long checkedAt) {
            this.stamp = stamp;
            this.checkedAt = checkedAt;
        }
    }

    @Inject
    private ResourceResolverFactory resolverFactory;

    private Declaration declaration;

    public PerComponentModel(Resource r) {
        super(r);
    }

    @Override
    @JsonIgnore(value = false)
    public List<IComponent> getChildren() {
        return declaration().container ? super.getChildren() : null;
    }

    @JsonAnyGetter
    public Map<String, Object> declaredValues() {
        final Declaration declared = declaration();
        final Map<String, Object> out = new LinkedHashMap<>();
        final Map<String, Object> properties = getResource().getValueMap();
        for (final String name : declared.scalars) {
            final Object value = properties.get(name);
            if (value != null) {
                out.put(name, value);
            }
        }
        for (final String name : declared.collections) {
            final Resource itemsNode = getResource().getChild(name);
            if (itemsNode != null) {
                final List<Map<String, Object>> items = new ArrayList<>();
                for (final Resource item : itemsNode.getChildren()) {
                    // Emit the item as a plain map read through getValueMap()
                    // - the ONE access path that is correct on every resource
                    // flavor. Under a version label, collection children can
                    // surface as RAW frozen nodes (nt:frozenNode, version
                    // storage paths); adapting those to IComponent loses
                    // every property, while their ValueMap still carries the
                    // authored fields.
                    final Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", item.getName());
                    item.getValueMap().forEach((key, value) -> {
                        if (!key.startsWith("jcr:") && !key.startsWith("sling:")) {
                            entry.put(key, value);
                        }
                    });
                    items.add(entry);
                }
                out.put(name, items);
            }
        }
        return out;
    }

    private Declaration declaration() {
        if (declaration == null) {
            declaration = resolve();
        }
        return declaration;
    }

    private Declaration resolve() {
        final String resourceType = getResource().getResourceType();
        final Declaration cached = CACHE.get(resourceType);
        final long now = System.currentTimeMillis();
        if (cached != null && now - cached.checkedAt < REVALIDATE_MS) {
            return cached;
        }
        try (ResourceResolver service = PerUtil.loginService(resolverFactory, DECLARATIONS_SUB_SERVICE)) {
            Resource componentNode = service.getResource(resourceType);
            Resource file = null;
            boolean isModelJson = false;
            for (int depth = 0; componentNode != null && depth < MAX_SUPER_TYPE_DEPTH; depth++) {
                file = componentNode.getChild(MODEL_JSON);
                isModelJson = file != null;
                if (file == null) {
                    file = componentNode.getChild(DIALOG_JSON);
                }
                if (file != null) {
                    break;
                }
                final String superType = componentNode.getValueMap().get(RESOURCE_SUPER_TYPE, String.class);
                componentNode = superType == null ? null : service.getResource(superType);
            }
            final Declaration fresh;
            if (file == null) {
                // Nothing declared anywhere: baseline export PLUS children.
                // Declaration-less components (adminv2's console components,
                // hand-built stubs) keep their state in nt:unstructured child
                // nodes - the config/nav-items pattern - and those children
                // have always flowed through the export. Only DECLARED
                // components get the strict leaf-vs-container split, because
                // their declaration says which they are.
                fresh = new Declaration(0L, now);
                fresh.container = true;
            } else {
                final long stamp = lastModified(file);
                if (cached != null && cached.stamp == stamp) {
                    cached.checkedAt = now;
                    return cached;
                }
                fresh = parse(file, isModelJson, stamp, now);
            }
            CACHE.put(resourceType, fresh);
            return fresh;
        } catch (LoginException e) {
            LOG.warn("No service session for '{}' - exporting baseline only for '{}'",
                    DECLARATIONS_SUB_SERVICE, resourceType, e);
            return cached != null ? cached : new Declaration(0L, 0L);
        }
    }

    private static long lastModified(Resource file) {
        final Resource content = file.getChild(JCR_CONTENT_NODE);
        final Calendar modified = content == null
                ? null
                : content.getValueMap().get(JCR_LAST_MODIFIED, Calendar.class);
        return modified == null ? 0L : modified.getTimeInMillis();
    }

    private static Declaration parse(Resource file, boolean isModelJson, long stamp, long now) {
        final Declaration declaration = new Declaration(stamp, now);
        try (InputStream is = file.adaptTo(InputStream.class)) {
            if (is == null) {
                return declaration;
            }
            final JsonNode root = MAPPER.readTree(is);
            if (isModelJson) {
                parseModelJson(root, declaration);
            } else {
                parseDialogJson(root, declaration);
            }
        } catch (Exception e) {
            LOG.warn("Could not parse component declaration '{}' - exporting baseline only", file.getPath(), e);
        }
        return declaration;
    }

    /** The hatch fragment: definitions.[Name].{x-type, properties.[field].x-form-type} */
    private static void parseModelJson(JsonNode root, Declaration declaration) {
        final JsonNode definitions = root.path("definitions");
        if (!definitions.isObject() || !definitions.fields().hasNext()) {
            return;
        }
        final JsonNode definition = definitions.elements().next();
        declaration.container = "container".equals(definition.path("x-type").asText());
        definition.path("properties").fields().forEachRemaining(field -> {
            if (COLLECTION.equals(field.getValue().path("x-form-type").asText())) {
                declaration.collections.add(field.getKey());
            } else {
                declaration.scalars.add(field.getKey());
            }
        });
    }

    /** Fallback for components without a model.json: {groups[].fields[], fields[]}, each {model, type} */
    private static void parseDialogJson(JsonNode root, Declaration declaration) {
        root.path("groups").forEach(group -> parseDialogFields(group.path("fields"), declaration));
        parseDialogFields(root.path("fields"), declaration);
    }

    private static void parseDialogFields(JsonNode fields, Declaration declaration) {
        fields.forEach(field -> {
            final String name = field.path("model").asText();
            if (name.isEmpty()) {
                return;
            }
            if (COLLECTION.equals(field.path("type").asText())) {
                declaration.collections.add(name);
            } else {
                declaration.scalars.add(name);
            }
        });
    }

}
