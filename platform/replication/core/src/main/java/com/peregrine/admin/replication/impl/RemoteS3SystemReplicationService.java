package com.peregrine.admin.replication.impl;

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

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.peregrine.replication.ReferenceLister;
import com.peregrine.replication.Replication;
import com.peregrine.render.RenderService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.HTML_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.JSON_MIME_TYPE;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerConstants.SLASH;
import static com.peregrine.commons.util.PerUtil.RENDITIONS;
import static com.peregrine.commons.util.PerUtil.getMimeType;
import static com.peregrine.commons.util.PerUtil.getPrimaryType;
import static com.peregrine.commons.util.PerUtil.intoList;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.commons.util.PerUtil.splitIntoMap;
import static com.peregrine.commons.util.PerUtil.splitIntoProperties;

/**
 * This class replicates resources to a S3 Bucket
 */
@Component(
    configurationPolicy = ConfigurationPolicy.REQUIRE,
    service = Replication.class,
    immediate = true
)
@Designate(ocd = RemoteS3SystemReplicationService.Configuration.class, factory = true)
public class RemoteS3SystemReplicationService
    extends BaseFileReplicationService
{

    public static final String JSON = ".json";
    public static final String HTML = "html";
    public static final String AWS_S3_SYSTEM = "aws-s3-system://";
    public static final String CONNECTION_TO_S3_COULD_NOT_BE_ESTABLISHED = "Connection to S3 could not be established";

    @ObjectClassDefinition(
        name = "Peregrine: Remove S3 Replication Service",
        description = "Each instance provides the configuration for a Remove S3 System Replication"
    )
    @interface Configuration {
        @AttributeDefinition(
            name = "Name",
            description = "Name of the Replication Service",
            defaultValue = "localFS",
            required = true
        )
        String name();
        @AttributeDefinition(
            name = "Description",
            description = "Description of this Replication Service",
            required = true
        )
        String description();
        @AttributeDefinition(
            name = "AWS Access Key",
            description = "Access Key to the AWS S3 Bucket",
            required = true
        )
        String awsAccessKey();
        @AttributeDefinition(
            name = "AWS Secret Key",
            description = "Secret Key to the AWS S3 Bucket",
            required = true
        )
        String awsSecretKey();
        @AttributeDefinition(
            name = "AWS Region Name",
            description = "Name of the AWS S3 Region",
            required = true
        )
        String awsRegionName();
        @AttributeDefinition(
            name = "AWS Bucket Name",
            description = "Name of the AWS S3 Bucket",
            required = true
        )
        String awsBucketName();
        @AttributeDefinition(
            name = "Export Extensions",
            description = "List of Export Extension in the format of <extension>=<comma separated list of primary types>",
            required = true
        )
        String[] exportExtensions();
//AS TODO: Not sure if that is needed. Keep it around for now
//        @AttributeDefinition(
//            name = "Extensions Parameters",
//            description = "List of Extension Parameters in the format of <extension>=[<parameter name>:<parameter value>|]*. For now 'exportFolder' takes a boolean (false is default)",
//            required = false
//        )
//        String[] extensionParameters();
        @AttributeDefinition(
            name = "Mandatory Renditions",
            description = "List of all the required renditions that are replicated (if missing they are created)",
            required = false
        )
        String[] mandatoryRenditions();
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(BundleContext context, Configuration configuration) { setup(context, configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(BundleContext context, Configuration configuration) { setup(context, configuration); }

    private List<ExportExtension> exportExtensions = new ArrayList<>();
    private List<String> mandatoryRenditions = new ArrayList<>();
    private AmazonS3 s3;
    private Regions region;
    private String awsBucketName;
    private String awsAccessKey;
    private String awsSecretKey;
    private String awsRegionName;

    private void setup(BundleContext context, Configuration configuration) {
        log.trace("Create Remote S3 Replication Service with Name: '{}'", configuration.name());
        init(configuration.name(), configuration.description());
        log.debug("Extension: '{}'", configuration.exportExtensions());
        exportExtensions.clear();
        Map<String, List<String>> extensions = splitIntoMap(configuration.exportExtensions(), "=", "\\|");
        Map<String, List<String>> extensionParameters = new HashMap<>();
        for(Entry<String, List<String>> extension: extensions.entrySet()) {
            String name = extension.getKey();
            if(isNotEmpty(name)) {
                List<String> types = extension.getValue();
                log.trace("Extension Types: '{}'", types);
                if(types != null && !types.isEmpty()) {
                    List<String> parameters = extensionParameters.get(name);
                    boolean exportFolder = false;
                    if(parameters != null) {
                        String param = splitIntoProperties(parameters, ":").get("exportFolder") + "";
                        exportFolder = "true".equalsIgnoreCase(param);
                    }
                    exportExtensions.add(
                        new ExportExtension(name, types).setExportFolders(exportFolder)
                    );
                } else {
                    log.trace("Extension Types is null or empty for Extension Name: '{}'", name);
                    throw new IllegalArgumentException("Supported Types is empty for Extension: " + extension);
                }
            } else {
                log.warn("Configuration contained an empty extension");
            }
        }
        log.debug("Mandatory Renditions: '{}'", configuration.mandatoryRenditions());
        mandatoryRenditions = intoList(configuration.mandatoryRenditions());

        awsBucketName = configuration.awsBucketName();
        awsAccessKey = configuration.awsAccessKey();
        awsSecretKey = configuration.awsSecretKey();
        awsRegionName = configuration.awsRegionName();

        region = null;
        try {
            region = Regions.fromName(awsRegionName);
        } catch(IllegalArgumentException e) {
            log.error("Unknown Region Name: '{}'", awsRegionName);
            throw e;
        }
        connectS3();
    }

    @Reference
    @SuppressWarnings("unused")
    private ReferenceLister referenceLister;
    @Reference
    @SuppressWarnings("unused")
    private RenderService renderService;
    @Reference
    @SuppressWarnings("unused")
    ResourceResolverFactory resourceResolverFactory;

    @Override
    RenderService getRenderService() {
        return renderService;
    }

    @Override
    ReferenceLister getReferenceLister() {
        return referenceLister;
    }

    @Override
    boolean isFolderOnTarget(String path) {
        // Paths do not to be created so we return true her
        return true;
    }

    private PutObjectRequest createPutRequest(String bucketName, String key, String extension, String content) {
        return createPutRequest(bucketName, key, extension, content.getBytes());
    }

    private boolean connectS3() {
        boolean answer = false;
        try {
            s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(
                    new AWSCredentialsProvider() {
                        @Override
                        public AWSCredentials getCredentials() {
                            return new AWSCredentials() {
                                @Override
                                public String getAWSAccessKeyId() {
                                    return awsAccessKey;
                                }

                                @Override
                                public String getAWSSecretKey() {
                                    return awsSecretKey;
                                }
                            };
                        }

                        @Override
                        public void refresh() {
                        }
                    })
                .withRegion(region)
                .build();
            answer = true;
        } catch(SdkClientException e) {
            log.error("Login to S3 failed", e);
        }
        return answer;
    }

    private PutObjectRequest createPutRequest(String bucketName, String key, String extension, byte[] content) {
        if(SLASH.equals(key)) {
            return null;
        }
        if(key.startsWith(SLASH)) {
            key = key.substring(1);
        }
        String awsKey = key + (isNotEmpty(extension) ? "." + extension : "");
        return new PutObjectRequest(
            awsBucketName,
            awsKey,
            new ByteArrayInputStream(content == null ? new byte[0] : content),
            new ContentLengthObjectMetadata(content == null ? 0 : content.length)
        );
    }

    @Override
    File createTargetFolder(String path) throws ReplicationException {
        // Paths do not to be created so we do nothing here
        return null;
    }

    @Override
    List<ExportExtension> getExportExtensions() { return exportExtensions; }

    @Override
    List<String> getMandatoryRenditions() {
        return mandatoryRenditions;
    }

    @Override
    String storeRendering(Resource resource, String extension, String content) throws ReplicationException {
        PutObjectRequest request = createPutRequest(awsBucketName, resource.getPath(), extension, content);
        if(extension.endsWith(JSON)) {
            log.trace("Set JSon Content Type");
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(JSON_MIME_TYPE);
            request.setMetadata(objectMetadata);
        } else if(extension.endsWith(HTML)) {
            log.trace("Set HTML Content Type");
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(HTML_MIME_TYPE);
            request.setMetadata(objectMetadata);
        }
        try {
            s3.putObject(request);
        } catch(SdkClientException e) {
            // Try to reconnect and try again
            if(connectS3()) {
                s3.putObject(request);
            } else {
                throw new ReplicationException(CONNECTION_TO_S3_COULD_NOT_BE_ESTABLISHED, e);
            }
        }
        log.trace("Send String Request to S3. Resource: '{}', Extension: '{}', Content: '{}'", resource.getPath(), extension, content);
        return AWS_S3_SYSTEM + resource.getPath();
    }

    private static class ContentLengthObjectMetadata
        extends ObjectMetadata
    {
        public ContentLengthObjectMetadata(int contentLength) {
            setContentLength(contentLength);
        }
    }

    @Override
    String storeRendering(Resource resource, String extension, byte[] content) throws ReplicationException {
        PutObjectRequest request = createPutRequest(awsBucketName, resource.getPath(), extension,content);
        // Check if this is an Asset and if so check for the rendition
        String mimeType = null;
        String primaryType = getPrimaryType(resource);
        if(ASSET_PRIMARY_TYPE.equals(primaryType) || NT_FILE.equals(primaryType)) {
            if(isNotEmpty(extension)) {
                Resource renditions = resource.getChild(RENDITIONS);
                if(renditions != null) {
                    Resource rendition = renditions.getChild(extension);
                    if(rendition != null) {
                        mimeType = getMimeType(rendition);
                    }
                }
            }
        }
        if(mimeType == null) {
            mimeType = getMimeType(resource);
        }
        if(isNotEmpty(mimeType)) {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            log.trace("Set Property Mime Type: '{}'", mimeType);
            objectMetadata.setContentType(mimeType);
            request.setMetadata(objectMetadata);
        }
        log.trace("Send Byte Request to S3. Resource: '{}', Extension: '{}', Content Length: '{}'", resource.getPath(), extension, content.length);
        try {
            s3.putObject(request);
        } catch(SdkClientException e) {
            // Try to reconnect and try again
            if(connectS3()) {
                s3.putObject(request);
            } else {
                throw new ReplicationException(CONNECTION_TO_S3_COULD_NOT_BE_ESTABLISHED, e);
            }
        }
        return AWS_S3_SYSTEM + resource.getPath();
    }

    @Override
    void removeReplica(Resource resource, final List<Pattern> namePattern, final boolean isFolder) throws ReplicationException {
        if(isFolder) {
            try {
                s3.deleteObject(new DeleteObjectRequest(awsBucketName, resource.getPath()));
            } catch(SdkClientException e) {
                // Try to reconnect and try again
                if(connectS3()) {
                    s3.deleteObject(new DeleteObjectRequest(awsBucketName, resource.getPath()));
                } else {
                    throw new ReplicationException(CONNECTION_TO_S3_COULD_NOT_BE_ESTABLISHED, e);
                }
            }
        } else {
            // List all objects of the resource parent paths and then match them with the pattern
//            String resourceName = resource.getName();
            Resource parent = resource.getParent();
            String parentFolderPath = "";
            if(parent != null) {
                parentFolderPath = parent.getPath();
            }
            ObjectListing objectListing;
            try {
                objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(awsBucketName).withPrefix(parentFolderPath));
            } catch(SdkClientException e) {
                // Try to reconnect and try again
                if(connectS3()) {
                    objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(awsBucketName).withPrefix(parentFolderPath));
                } else {
                    throw new ReplicationException(CONNECTION_TO_S3_COULD_NOT_BE_ESTABLISHED, e);
                }
            }
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                String key = objectSummary.getKey();
                String name = key.substring(parentFolderPath.length());
                if(name.startsWith(SLASH)) {
                    name = name.length() > 1 ? name.substring(1) : "";
                }
                if(namePattern == null) {
                    s3.deleteObject(new DeleteObjectRequest(awsBucketName, key));
                } else {
                    for(Pattern pattern : namePattern) {
                        if(pattern.matcher(name).matches()) {
                            s3.deleteObject(new DeleteObjectRequest(awsBucketName, key));
                        }
                    }
                }
            }
        }
    }
}
