<%@ page session="false" contentType="text/plain;charset=UTF-8" pageEncoding="UTF-8"
    import="org.apache.sling.api.resource.Resource,org.apache.sling.api.resource.ValueMap" %>
<%@ taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<sling:defineObjects/>
<%
  // per:Page entry point for the .md view. Front matter, then each content
  // component renders itself (Sling resolves its own md.jsp, or the generic
  // property-table default).
  Resource content = resource.getChild("jcr:content");
  if (content == null) content = resource;
  ValueMap p = content.getValueMap();
  out.write("# " + p.get("jcr:title", resource.getName()) + "\n\n");
  String d = p.get("jcr:description", "");
  if (!d.isEmpty()) out.write(d.replaceAll("<[^>]+>", " ").trim() + "\n\n");
  out.write("<!-- source: " + resource.getPath() + " -->\n");
  for (Resource c : content.getChildren()) {
    String n = c.getName();
    if (n.startsWith("jcr:") || n.startsWith("rep:")) continue;
    out.flush();
    slingRequest.getRequestDispatcher(c).include(slingRequest, slingResponse);
  }
  out.write("\n");
%>
