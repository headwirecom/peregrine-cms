# Sling JUnit Tests

These test rely on the content deployed from `samples/example-vue-site`
This is the "example" internal site. 

Deploy the example site
* cd samples/example-vue-site
* mvn clean install -PautoDeployPackage

Deploy Sling Junit Package
* cd slingjunit.parent
* mvn clean install -PautoDeployPackage

Run all the tests if you are using `remote` distribution and have both author publish instances running
* http://localhost:8080/system/sling/junit/

Otherwise run specific tests
* /system/sling/junit/com.peregrine.slingjunit.VersionsJTest.html

## Notes:
1. org.apache.sling.junit.core does need to be in the allowlist for Login Administrative.
Deploying the bundle directly from peregrine-cms/slingjunit.parent will provide that configuration.  
   *  mvn clean install -PautoDeployPackage
2. *RemoteReplAuthorJTest* is a test suite to check remote replication setup (/system/sling/junit/com.peregrine.slingjunit.author.RemoteReplAuthorJTest.html)
   * For this test to pass, configurations for other replication services (LocalReplicationService and LocalFileSystemReplicationService) may need to be removed. 
   * If this is run outside of the localhost environment, make sure the default transport user credentials are updated, see UserCredentialsDistributionTransportSecretProvider osgi config
   * It may be necessary to verify the permissions for service users `distribution-agent-user` and `defaultAgentService` are matching org.apache.sling.jcr.repoinit.RepositoryInitializer~peregrine.json
3. *PermissionJTest* will fail as everyone has read access on /content by default as a result of base.json (line 335). 
admins may find it more appropriate to remove this ACE depending on their specific requirements. 