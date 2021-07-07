#!/bin/bash

# run the docker instance we just built
docker run -d --rm -p 8080:8080 --name peregrine peregrinecms/peregrine-cms:latest

# install the sling junit test suite into the docker image and execute it

cd ..
cd slingjunit.parent
mvn install -PautoInstallPackage

sleep 15

curl -s "http://admin:admin@localhost:8080/system/sling/junit/"
curl -s -X POST "http://admin:admin@localhost:8080/system/sling/junit/.json" | jq . | grep "\"failure\":"
cd ../docker

# cleanup docker
docker kill peregrine
