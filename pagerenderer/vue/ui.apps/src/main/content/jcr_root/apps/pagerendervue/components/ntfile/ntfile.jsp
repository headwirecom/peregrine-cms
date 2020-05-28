<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling" %>
<sling:defineObjects />
<%= resource.getChild("jcr:content/jcr:data").adaptTo(String.class) %>