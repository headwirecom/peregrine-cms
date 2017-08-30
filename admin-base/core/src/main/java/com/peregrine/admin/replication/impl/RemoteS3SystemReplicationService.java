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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.peregrine.admin.replication.ReferenceLister;
import com.peregrine.admin.replication.Replication;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.peregrine.commons.util.PerConstants.ASSET_PRIMARY_TYPE;
import static com.peregrine.commons.util.PerConstants.NT_FILE;
import static com.peregrine.commons.util.PerUtil.RENDITIONS;
import static com.peregrine.commons.util.PerUtil.getMimeType;
import static com.peregrine.commons.util.PerUtil.getPrimaryType;
import static com.peregrine.commons.util.PerUtil.intoList;
import static com.peregrine.commons.util.PerUtil.isNotEmpty;
import static com.peregrine.commons.util.PerUtil.splitIntoMap;

/**
 * This class replicates resources to a local file system folder
 * by exporting its content
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
        @AttributeDefinition(
            name = "Mandatory Renditions",
            description = "List of all the required renditions that are replicated (if missing they are created)",
            required = true
        )
        String[] mandatoryRenditions();
    }

    @Activate
    @SuppressWarnings("unused")
    void activate(BundleContext context, Configuration configuration) { setup(context, configuration); }
    @Modified
    @SuppressWarnings("unused")
    void modified(BundleContext context, Configuration configuration) { setup(context, configuration); }

    private Map<String, List<String>> exportExtensions = new HashMap<>();
    private List<String> mandatoryRenditions = new ArrayList<>();
    private AmazonS3 s3;
    private String awsBucketName;
    private String awsAccessKey;
    private String awsSecretKey;
    private String awsRegionName;

    private void setup(BundleContext context, final Configuration configuration) {
        init(configuration.name(), configuration.description());
        exportExtensions = splitIntoMap(configuration.exportExtensions(), "=", "\\|");
        mandatoryRenditions = intoList(configuration.mandatoryRenditions());

        awsBucketName = configuration.awsBucketName();
        awsAccessKey = configuration.awsAccessKey();
        awsSecretKey = configuration.awsSecretKey();
        awsRegionName = configuration.awsRegionName();

        Regions region = null;
        try {
            region = Regions.fromName(awsRegionName);
        } catch(IllegalArgumentException e) {
            log.error("Unknown Region Name: '{}'", awsRegionName);
            throw e;
        }
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
        } catch(SdkClientException e) {
            log.error("Login to S3 failed", e);
            throw e;
        }
    }

    @Reference
    @SuppressWarnings("unused")
    private ReferenceLister referenceLister;
    @Reference
    @SuppressWarnings("unused")
    private SlingRequestProcessor requestProcessor;
    @Reference
    @SuppressWarnings("unused")
    ResourceResolverFactory resourceResolverFactory;

    @Override
    SlingRequestProcessor getRequestProcessor() {
        return requestProcessor;
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

    private PutObjectRequest createPutRequest(String bucketName, String key, String extension, byte[] content) {
        if("/".equals(key)) {
            return null;
        }
        if(key.startsWith("/")) {
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
    void createTargetFolder(String path) throws ReplicationException {
        // Paths do not to be created so we do nothing here
    }

    @Override
    Map<String, List<String>> getExportExtensions() { return exportExtensions; }

    @Override
    List<String> getMandatoryRenditions() {
        return mandatoryRenditions;
    }

    @Override
    String storeRendering(Resource resource, String extension, String content) throws ReplicationException {
        PutObjectRequest request = createPutRequest(awsBucketName, resource.getPath(), extension,content);
        if(extension.endsWith(".json")) {
            log.trace("Set JSon Content Type");
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("application/json");
            request.setMetadata(objectMetadata);
        } else if(extension.endsWith("html")) {
            log.trace("Set HTML Content Type");
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("text/html");
            request.setMetadata(objectMetadata);
        }
        s3.putObject(request);
        return "aws-s3-system://" + resource.getPath();
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
        s3.putObject(request);
        return "aws-s3-system://" + resource.getPath();
    }

    @Override
    void removeReplica(Resource resource, final List<Pattern> namePattern, final boolean isFolder) throws ReplicationException {
        if(isFolder) {
            s3.deleteObject(new DeleteObjectRequest(awsBucketName, resource.getPath()));
        } else {
            // List all objects of the resource parent paths and then match them with the pattern
            String resourceName = resource.getName();
            Resource parent = resource.getParent();
            String parentFolderPath = "";
            if(parent != null) {
                parentFolderPath = parent.getPath();
            }
            ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(awsBucketName).withPrefix(parentFolderPath));
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                String key = objectSummary.getKey();
                String name = key.substring(parentFolderPath.length());
                if(name.startsWith("/")) {
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
