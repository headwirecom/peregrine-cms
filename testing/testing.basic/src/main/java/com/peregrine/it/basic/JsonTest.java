package com.peregrine.it.basic;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.peregrine.commons.util.PerConstants.ASSET_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerConstants.JCR_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.JCR_TITLE;
import static com.peregrine.commons.util.PerConstants.NT_UNSTRUCTURED;
import static com.peregrine.commons.util.PerConstants.OBJECT_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_CONTENT_TYPE;
import static com.peregrine.commons.util.PerConstants.PAGE_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.SLING_RESOURCE_TYPE;
import static com.peregrine.commons.util.PerUtil.TEMPLATE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class JsonTest {

    private static final Logger logger = LoggerFactory.getLogger(JsonTest.class.getName());

    public static interface Basic {
        public String toJSon() throws IOException;

        public String getName();
        public Map<String, Basic> getChildren();
        public Map<String, Prop> getProperties();
    }

    public static class BasicImpl implements Basic {
        private String name;
        private Map<String, Basic> children = new LinkedHashMap<String, Basic>();
        private Map<String, Prop> properties = new LinkedHashMap<String, Prop>();

        public BasicImpl(String name) {
            setName(name);
        }

        public BasicImpl(BasicImpl source) {
            this.name = source.getName();
            for(Entry<String, Basic> entry: source.children.entrySet()) {
                String key = entry.getKey();
                Basic value = entry.getValue();
                try {
                    Constructor constructor = value.getClass().getConstructor(value.getClass());
                    Basic copy = (Basic) constructor.newInstance(value);
                    children.put(entry.getKey(), copy);
                } catch(NoSuchMethodException e) {
                    logger.error("Could not find Copy Constructor for instance: '{}'", value);
                } catch(IllegalAccessException e) {
                    logger.error("Could not access Copy Constructor for instance: '{}'", value);
                } catch(InstantiationException e) {
                    logger.error("Could not instantiate Copy Constructor for instance: '{}'", value);
                } catch(InvocationTargetException e) {
                    logger.error("Could not invoke Copy Constructor for instance: '{}'", value);
                }
            }
            for(Entry<String, Prop> entry: source.properties.entrySet()) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }

        public void setName(String name) {
            if(isEmpty(name)) { throw new IllegalArgumentException("Object Name must be provided"); }
            this.name = name;
        }

        @Override
        public String getName() { return name; }

        @Override
        public Map<String, Basic> getChildren() { return children; }

        @Override
        public Map<String, Prop> getProperties() { return properties; }

        public BasicImpl addChildren(Basic...children) {
            for(Basic child: children) {
                addChild(child);
            }
            return this;
        }

        public BasicImpl addChild(Basic child) {
            String name = child.getName();
            children.put(name, child);
            return this;
        }

        public BasicImpl addProperties(Prop...props) {
            for(Prop prop: props) {
                addProperty(prop);
            }
            return this;
        }

        public BasicImpl addProperty(Prop prop) {
            String name = prop.getName();
            if(name == null || name.isEmpty()) { throw new IllegalArgumentException("Property Name must be provided"); }
            properties.put(name, prop);
            return this;
        }

        @Override
        public String toJSon() throws IOException {
            StringWriter writer = new StringWriter();
            JsonFactory jf = new JsonFactory();
            JsonGenerator json = jf.createGenerator(writer);
            logger.info("Start Writing JSon for: '{}'", name);
            json.writeStartObject();
            for(Entry<String, Prop> entry: properties.entrySet()) {
                logger.info("Write Property with Name: '{}'", entry.getKey());
                String propertyValue = entry.getValue().getJSonValue();
                logger.info("Write Property with Value: '{}'", propertyValue);
                json.writeFieldName(entry.getKey());
                json.writeRawValue(propertyValue);
            }
            for(Entry<String, Basic>entry: children.entrySet()) {
                logger.info("Write Child with Name: '{}'", entry.getKey());
                String childJSon = entry.getValue().toJSon();
                logger.info("Write Child, with Value: '{}'", childJSon);
                json.writeFieldName(entry.getKey());
                json.writeRawValue(childJSon);
            }
            json.writeEndObject();
            json.close();
            String answer = writer.toString();
            logger.info("Return Object: '{}'", answer);
            return answer;
        }
    }

    public static class BasicListObject extends BasicImpl {
        List<String> list = new ArrayList<String>();
        public BasicListObject(String name, BasicImpl...children) {
            super(name);
            addChildren(children);
        }
        public BasicListObject(String name, String...singleListItems) {
            super(name);
            if(singleListItems != null) {
                list.addAll(Arrays.asList(singleListItems));
            }
        }
        public BasicListObject(BasicListObject source) {
            super(source);
            for(String item: source.list) {
                list.add(item);
            }
        }
        @Override
        public String toJSon() throws IOException {
            StringWriter writer = new StringWriter();
            JsonFactory jf = new JsonFactory();
            JsonGenerator json = jf.createGenerator(writer);
            logger.info("Start Writing JSon for: '{}'", getName());
            Map<String, Basic> children = getChildren();
            if(!children.isEmpty()) {
                json.writeStartArray();
                for(Entry<String, Basic> entry : getChildren().entrySet()) {
                    logger.info("Write Child with Name: '{}'", entry.getKey());
                    String childJSon = entry.getValue().toJSon();
                    logger.info("Write Child, with Value: '{}'", childJSon);
                    json.writeRawValue(childJSon);
                }
                json.writeEndArray();
            }
            if(!list.isEmpty()) {
                json.writeStartArray();
                for(String item: list) {
                    json.writeString(item);
                }
                json.writeEndArray();
            }
            json.close();
            String answer = writer.toString();
            logger.info("Return Object: '{}'", answer);
            return answer;
        }
    }

    public static class BasicObject extends BasicImpl {
        public BasicObject(String name, String primaryType) {
            super(name);
            addProperty(new Prop(JCR_PRIMARY_TYPE, primaryType));
        }
        public BasicObject(BasicObject source) {
            super(source);
        }
        public BasicObject addSlingResourceType(String slingResourceType) {
            addProperty(new Prop(SLING_RESOURCE_TYPE, slingResourceType));
            return this;
        }
    }

    public static class NoNameObject extends BasicObject {
        public NoNameObject(String primaryType) {
            super(null, primaryType);
        }
        public NoNameObject(NoNameObject source) { super(source); }

        @Override
        public void setName(String name) {
        }
    }

    public static class BasicContentObject extends BasicObject {
        public BasicContentObject(String primaryType, String slingResourceType) {
            super(JCR_CONTENT, primaryType);
            if(slingResourceType != null) {
                addSlingResourceType(slingResourceType);
            }
        }
        public BasicContentObject(BasicContentObject source) {
            super(source);
        }
    }

    public static class BasicWithContent extends BasicObject {
        public BasicWithContent(String name, String primaryType, String contentPrimaryType, String slingResourceType) {
            super(name, primaryType);
            addChild(new BasicContentObject(contentPrimaryType, slingResourceType));
        }
        public BasicWithContent(BasicWithContent source) {
            super(source);

        }
        public BasicContentObject getContent() { return (BasicContentObject) getChildren().get(JCR_CONTENT); }

        public BasicWithContent addContentProperty(Prop prop) { getContent().addProperty(prop); return this; }
    }

    public static class TestTemplate extends BasicWithContent {
        public TestTemplate(String name, String slingResourceType) {
            super(name, PAGE_PRIMARY_TYPE, PAGE_CONTENT_TYPE, slingResourceType);
            addContentProperty(new Prop(JCR_TITLE, name));
        }
        public TestTemplate(TestTemplate source) {
            super(source);
        }
        public TestTemplate addContentChildren(Basic...children) {
            getContent().addChildren(children);
            return this;
        }
    }

    public static class TestPage extends TestTemplate {
        public TestPage(String name, String slingResourceType, String templatePath) {
            super(name, slingResourceType);
            if(templatePath != null) {
                addContentProperty(new Prop(TEMPLATE, templatePath));
            }
        }
        public TestPage(TestPage source) {
            super(source);
        }
    }

    public static class TestAsset extends BasicWithContent {
        public TestAsset(String mimeType) {
            this(null, mimeType);
        }
        public TestAsset(String name, String mimeType) {
            super(name, ASSET_PRIMARY_TYPE, ASSET_CONTENT_TYPE, null);
            addContentProperty(new Prop(JCR_MIME_TYPE, mimeType));
        }
        public TestAsset(TestAsset source) {
            super(source);
        }
        // This is overridden to avoid a NPE because of the missing name
        @Override
        public void setName(String name) {
            if(name != null) {
                super.setName(name);
            }
        }
    }

    public static class ObjectComponent extends BasicObject {
        public ObjectComponent(String name, String slingResourceType) {
            this(name, slingResourceType, null);
        }
        public ObjectComponent(String name, String slingResourceType, Prop...props) {
            super(name, OBJECT_PRIMARY_TYPE);
            addSlingResourceType(slingResourceType);
            if(props != null) {
                addProperties(props);
            }
        }
        public ObjectComponent(ObjectComponent source) { super(source); }
    }

    public static class ChildObject extends BasicObject {
        public ChildObject(String name, String slingResourceType) {
            this(name, NT_UNSTRUCTURED, slingResourceType);
        }

        public ChildObject(String name, String primaryType, String slingResourceType) {
            super(name, primaryType);
            addSlingResourceType(slingResourceType);
        }
        public ChildObject(ChildObject source) { super(source); }
    }

    public static class Prop {
        private String name, value;
        private List<String> list;

        public Prop(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Prop(String name, String...values) {
            this.name = name;
            if(values != null) {
                this.list = new ArrayList<String>(Arrays.asList(values));
            }
        }

        public String getName() { return name; }

        public String getJSonValue() {
            String answer = "";
            if(list == null) {
                answer = "\"" + value + "\"";
            } else {
                answer = "[";
                if(list.isEmpty()) {
                    answer += "\"\"";
                } else {
                    for(String item : list) {
                        answer += "\"" + item + "\",";
                    }
                    answer = answer.substring(0, answer.length() - 1);
                }
                answer += "]";
            }
            return answer;
        }
    }
}
