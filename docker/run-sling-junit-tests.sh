#!/bin/bash

# run the docker instance we just built
docker run -d --rm -p 8080:8080 --name peregrine peregrinecms/peregrine-cms:latest

# Wait for Sling to fully start up
STATUS=$(curl -u admin:admin -s --fail  http://localhost:8080/system/console/bundles.json | jq '.s[3:5]' -c)
if [ "$STATUS" != "[0,0]" ]; then
  while [ "$STATUS" != "[0,0]" ]
  do    
    echo "Sling still starting. Waiting for all bundles to be ready.."
    STATUS=$(curl -u admin:admin -s --fail  http://localhost:8080/system/console/bundles.json | jq '.s[3:5]' -c)
    sleep 5
  done
fi

# install the sling junit test suite into the docker image and execute it

cd ..
cd slingjunit.parent
mvn install -PautoInstallPackage
curl -s -X POST http://admin:admin@localhost:8080/system/sling/junit/.json | jq . | grep "\"failure\":"
cd ../docker

# cleanup docker
docker kill peregrine
