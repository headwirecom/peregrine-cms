package com.peregrine.commons.test.mock;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.mockito.stubbing.Answer;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.peregrine.commons.util.PerConstants.SLASH;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceMock extends ResourceWrapper {

    protected static final String DEFAULT_NAME = null;

    protected final String debugName;

    protected final Resource mock;
    protected final Node node;

    protected final Map<String, Object> properties = new HashMap<>();

    private final Map<String, ResourceMock> children = new LinkedHashMap<>();

    private final Map<Class, Object> adaptTo = new LinkedHashMap<>();

    private String path;
    private ResourceResolver resourceResolver;
    private Session session;

    public ResourceMock(final String debugName) {
        super(mock(Resource.class, debugName));
        this.debugName = debugName;
        mock = getResource();
        node = mockNode(debugName);
        final ValueMap valueMap = new ValueMapDecorator(properties);
        when(mock.getValueMap()).thenReturn(valueMap);
    }

    public ResourceMock() {
        this(DEFAULT_NAME);
    }

    private Node mockNode(final String name) {
        final Node mock = mock(Node.class, concatenateToDerivedName(name, " Node"));
        addAdapter(mock);
        final Answer<Property> setPropertyAnswer = invocation -> {
            final Object[] arguments = invocation.getArguments();
            final String key = (String) arguments[0];
            final Object value = arguments[1];
            properties.put(key, value);
            return mockNodeProperty(key);
        };
        try {
            when(mock.hasProperty(anyString())).then(invocation ->
                    properties.containsKey((invocation.getArguments()[0])));
            when(mock.setProperty(anyString(), anyString())).then(setPropertyAnswer);
            when(mock.getProperty(anyString())).then(invocation ->
                    mockNodeProperty((String) invocation.getArguments()[0]));
            when(mock.getProperties()).thenReturn(new PropertyIteratorMock());
            when(mock.getNodes()).thenReturn(new NodeIteratorMock(children));
            when(mock.hasNode(anyString())).then(invocation -> children.containsKey(invocation.getArguments()[0]));
            when(mock.getNode(anyString())).then(invocation -> children.get(invocation.getArguments()[0]).getNode());
        } catch (final RepositoryException e) { }

        return mock;
    }

    private Property mockNodeProperty(final String key) {
        final Property mock = mock(Property.class, concatenateToDerivedName("Property '", key, "'"));
        try {
            when(mock.getNode()).thenReturn(node);
            when(mock.getName()).thenReturn(key);
            final Object value = properties.get(key);
            if (value != null) {
                if (value instanceof String) {
                    when(mock.getString()).thenReturn((String) value);
                }
            }

        } catch (final RepositoryException e) {
        }

        return mock;
    }

    protected static final String concatenateToDerivedName(final Object... objects) {
        final StringBuilder builder = new StringBuilder();
        for (final Object s : objects) {
            if (s == null) {
                return DEFAULT_NAME;
            }

            builder.append(s);
        }

        return StringUtils.trim(builder.toString());
    }

    public final ResourceMock setResourceResolver(final ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
        when(mock.getResourceResolver()).thenReturn(resourceResolver);
        updateResourceResolverGetResource();
        updateChildren();
        return this;
    }

    private void updateResourceResolverGetResource() {
        if (resourceResolver != null) {
            when(resourceResolver.getResource(path)).thenReturn(this);
        }
    }

    private void updateChildren() {
        for (final ResourceMock child : children.values()) {
            updateChild(child);
        }
    }

    private void updateChild(final ResourceMock child) {
        if (resourceResolver != null) {
            child.setResourceResolver(resourceResolver);
        }

        if (session != null) {
            child.setSession(session);
        }
    }

    public final Map<String, Object> getProperties() {
        return properties;
    }

    public final ResourceMock putProperty(final String name, final Object property) {
        properties.put(name, property);
        return this;
    }

    public Object getProperty(final String name) {
        return properties.get(name);
    }

    public final ResourceMock setPath(final String path) {
        this.path = path;
        when(mock.getPath()).thenReturn(path);
        try {
            when(node.getPath()).thenReturn(path);
        } catch (final RepositoryException e) { }
        updateResourceResolverGetResource();
        setPathImpl(path);
        return this;
    }

    protected void setPathImpl(final String path) { }

    @Override
    public final String getName() {
        return StringUtils.substringAfterLast(path, SLASH);
    }

    public final ResourceMock setParent(final Resource parent) {
        when(mock.getParent()).thenReturn(parent);
        return this;
    }

    @Override
    public final Resource getChild(final String name) {
        return children.get(name);
    }

    public final ResourceMock addChild(final String name, final ResourceMock child) {
        children.put(name, child);
        updateChild(child);
        return this;
    }

    public final ResourceMock addChild(final ResourceMock child) {
        addChild(child.getName(), child);
        return this;
    }

    @Override
    public Iterable<Resource> getChildren() {
        return children.values().stream().collect(Collectors.toList());
    }

    @Override
    public Iterator<Resource> listChildren() {
        return getChildren().iterator();
    }

    @Override
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void addAdapter(final Object adapter) {
        adaptTo.put(adapter.getClass(), adapter);
    }

    @Override
    public <AdapterType> AdapterType adaptTo(final Class<AdapterType> type) {
        if (adaptTo.containsKey(type)) {
            return (AdapterType) adaptTo.get(type);
        }

        for (final Map.Entry<Class, Object> entry : adaptTo.entrySet()) {
            if (type.isAssignableFrom(entry.getKey())) {
                return (AdapterType) entry.getValue();
            }
        }

        return super.adaptTo(type);
    }

    public Node getNode() {
        return node;
    }

    public ResourceMock setSession(final Session session) {
        this.session = session;
        try {
            when(node.getSession()).thenReturn(session);
            when(session.getNode(path)).thenReturn(node);
            when(session.itemExists(path)).thenReturn(true);
            when(session.nodeExists(path)).thenReturn(true);
        } catch (final RepositoryException e) { }
        updateChildren();
        return this;
    }

    private final class PropertyIteratorMock extends PropertyIteratorMockBase {

        public PropertyIteratorMock() {
            super(properties.keySet().iterator());
        }

        @Override
        public Property nextProperty() {
            return mockNodeProperty(keys.next());
        }
    }

}
