<%@ page session="false" trimDirectiveWhitespaces="true" contentType="text/plain;charset=UTF-8" pageEncoding="UTF-8"
    import="org.apache.sling.api.resource.Resource,org.apache.sling.api.resource.ValueMap,java.util.Map" %>
<%@ taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<sling:defineObjects/>
<%!
  static String cell(Object v) {
    String s;
    if (v instanceof String[]) { String[] a=(String[])v; s=String.join(", ", a); }
    else s = String.valueOf(v);
    return s.replaceAll("<[^>]+>"," ").replace("\n"," ").replace("|","\\|").replaceAll("[ \\t]+"," ").trim();
  }
%>
<%
  // Generic fallback: a component with no md.jsp of its own renders its
  // authored properties as a Markdown table, then its children recurse.
  ValueMap vm = resource.getValueMap();
  String rt = vm.get("sling:resourceType", "");
  String comp = rt.contains("/") ? rt.substring(rt.lastIndexOf('/') + 1) : rt;
  StringBuilder tb = new StringBuilder();
  for (Map.Entry<String,Object> e : vm.entrySet()) {
    String k = e.getKey();
    if (k.startsWith("jcr:") || k.startsWith("sling:") || k.startsWith(":") || k.equals("component")) continue;
    String val = cell(e.getValue());
    if (val.isEmpty()) continue;
    tb.append("| ").append(k).append(" | ").append(val).append(" |\n");
  }
  if (tb.length() > 0) {
    if (!comp.isEmpty()) out.write("\n### " + comp + "\n\n");
    out.write("| field | value |\n|---|---|\n" + tb.toString() + "\n");
  }
  for (Resource c : resource.getChildren()) {
    String n = c.getName();
    if (n.startsWith("jcr:") || n.startsWith("rep:")) continue;
    out.flush();
    slingRequest.getRequestDispatcher(c).include(slingRequest, slingResponse);
  }
%>
