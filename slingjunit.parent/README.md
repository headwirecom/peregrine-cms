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