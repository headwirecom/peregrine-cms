# install the sling junit test suite into the docker image and execute it

cd ..
cd slingjunit.parent
mvn install -PautoInstallPackage
curl -s -X POST http://localhost:8080/system/sling/junit/.json | jq . | grep "\"failure\":"
cd docker