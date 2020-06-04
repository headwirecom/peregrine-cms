package com.peregrine.admin.servlets;

/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.peregrine.commons.servlets.AbstractBaseServlet;
import com.peregrine.intra.IntraSlingCaller.CallException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.peregrine.admin.util.AdminConstants.CHARSET;
import static com.peregrine.admin.util.AdminConstants.CHARSET_NAME;
import static com.peregrine.admin.util.AdminConstants.JOB_TOPIC;
import static com.peregrine.admin.util.AdminConstants.JOB_TOPIC_NAME;
import static com.peregrine.admin.util.AdminConstants.OPERATION_NAME;
import static com.peregrine.admin.util.AdminConstants.PACKAGE_FORMAT;
import static com.peregrine.admin.util.AdminConstants.REFERENCE_NAME;

/**
 * Abstract Base Servlet Class for Tenant Backup and Restore Servlets
 */

@SuppressWarnings("serial")
public abstract class AbstractPackageServlet extends AbstractBaseServlet {

    public static final String JOB_OUTFILE_PROPERTY = "outfile";

    abstract Packaging getPackaging();
    abstract JobManager getJobManager();

    /**
     * Retrieves a package manager for the JCR session.
     */
    JcrPackageManager getPackageManager(Request request) throws RepositoryException {
        ResourceResolver resolver = request.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);
        if (session != null) {
            return getPackaging().getPackageManager(session);
        } else {
            throw new RepositoryException("can't adapt resolver to session"); // should be impossible
        }
    }

    JcrPackage getJcrPackage(JcrPackageManager manager, Resource resource) throws RepositoryException {
        JcrPackage jcrPackage = null;
        Node node;
        if (isValidResource(resource) && (node = resource.adaptTo(Node.class)) != null) {
            jcrPackage = manager.open(node, true);
        }
        return jcrPackage;
    }

    boolean isValidResource(Resource resource) {
        return resource != null && resource.getResourceResolver().getResource(resource.getPath()) != null;
    }


    JsonNode executeJob(
        ResourceResolver resourceResolver, Map<String, String> parameters, String tenantName, String operation
    ) throws CallException, IOException {
        final JackrabbitSession session = (JackrabbitSession) resourceResolver.adaptTo(Session.class);
        Map<String, Object> properties = new HashMap<>();

        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            properties.put(parameter.getKey(), parameter.getValue());
        }
        properties.put("userid", session.getUserID());
        properties.put(JOB_TOPIC, JOB_TOPIC_NAME);
        properties.put(REFERENCE_NAME, String.format(PACKAGE_FORMAT, tenantName));
        properties.put(CHARSET_NAME, CHARSET);
        properties.put(OPERATION_NAME, operation);

        buildOutfileName(properties);
        Job job = getJobManager().addJob(JOB_TOPIC_NAME, properties);
        ObjectNode answer = new ObjectMapper().createObjectNode();
        job2json(answer, job);
        return answer;
    }

    JsonNode getJobById(JobManager jobManager, ResourceResolver resolver, String jobId) throws IOException {
        ObjectNode answer = new ObjectMapper().createObjectNode();
        Job job = jobManager.getJobById(jobId);
        if (job == null) {
            //fallback: use audit
            final Iterator<Resource> resources = resolver.findResources("/jcr:root/var/audit/jobs//*[slingevent:eventId='" + jobId + "']", "xpath");
            if (resources.hasNext()) {
                final Resource audit = resources.next();
                audit2json(answer, audit);
            } else {
                answer = null;
            }
        } else {
            job2json(answer, job);
        }
        return answer;
    }

    private void job2json(ObjectNode jsonObject, Job job) throws IOException {
        Set<String> propertyNames = Collections.unmodifiableSet(job.getPropertyNames());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (String propertyName : propertyNames) {
            Object property = job.getProperty(propertyName);
            if (property instanceof Calendar) {
                dateFormat.setTimeZone(((Calendar) property).getTimeZone());
                jsonObject.put(propertyName, dateFormat.format(((Calendar) property).getTime()));
            } else if (property instanceof Boolean) {
                jsonObject.put(propertyName, (Boolean) property);
            } else if (property instanceof Long) {
                jsonObject.put(propertyName, (Long) property);
            } else if (property instanceof Number) {
                jsonObject.put(propertyName, (Integer) property);
            } else if (property instanceof String) {
                final String s = (String) property;
                if (propertyName.equals("outfile")) {
                    jsonObject.put(propertyName, s.substring(s.lastIndexOf('/') + 1));
                } else {
                    jsonObject.put(propertyName, s);
                }
            } else if (property instanceof Object[]) {
                ArrayNode arrayNode = jsonObject.putArray(propertyName);
                for (Object o : (Object[]) property) {
                    arrayNode.add(String.valueOf(o));
                }
            } else {
                jsonObject.put(propertyName, String.valueOf(property));
            }
        }
        jsonObject.put("jobState", job.getJobState().name());
    }

    private void audit2json(ObjectNode jsonObject, Resource audit) throws IOException {
        ValueMap properties = audit.getValueMap();
        Set<String> propertyNames = Collections.unmodifiableSet(properties.keySet());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (String propertyName : propertyNames) {
            Object property = properties.get(propertyName);
            if (property instanceof Calendar) {
                dateFormat.setTimeZone(((Calendar) property).getTimeZone());
                jsonObject.put(propertyName, dateFormat.format(((Calendar) property).getTime()));
            } else if (property instanceof Boolean) {
                jsonObject.put(propertyName, (Boolean) property);
            } else if (property instanceof Long) {
                jsonObject.put(propertyName, (Long) property);
            } else if (property instanceof Number) {
                jsonObject.put(propertyName, (Integer) property);
            } else if (property instanceof String) {
                final String s = (String) property;
                if (propertyName.equals(JOB_OUTFILE_PROPERTY)) {
                    jsonObject.put(propertyName, s.substring(s.lastIndexOf('/') + 1));
                } else {
                    jsonObject.put(propertyName, s);
                }
            } else if (property instanceof Object[]) {
                ArrayNode arrayNode = jsonObject.putArray(propertyName);
                for (Object o : (Object[]) property) {
                    arrayNode.add(String.valueOf(o));
                }
            } else {
                jsonObject.put(propertyName, String.valueOf(property));
            }
        }
    }

    private String buildOutfileName(Map<String, Object> properties) {
        String outfile = (String) properties.get(JOB_OUTFILE_PROPERTY);
        if (StringUtils.isBlank(outfile)) {
            String outfilePrefix = (String) properties.get("outfileprefix");
            final String tmpdir = System.getProperty("java.io.tmpdir");
            final boolean endsWithSeparator = (tmpdir.charAt(tmpdir.length() - 1) == File.separatorChar);
            outfile = tmpdir + (endsWithSeparator ? "" : File.separator)
                + (StringUtils.isBlank(outfilePrefix) ? "slingjob" : outfilePrefix)
                + "_" + System.currentTimeMillis() + ".out";
            properties.put(JOB_OUTFILE_PROPERTY, outfile);
        }
        return outfile;
    }
}
