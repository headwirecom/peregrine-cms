#!/bin/bash

# run the docker instance we just built
docker run -d --rm -p 8080:8080 --name peregrine peregrinecms/peregrine-cms:latest

# clone and install the e2e test repository
git clone https://github.com/peregrine-cms/peregrine-cms-e2e-testing.git
cd peregrine-cms-e2e-testing
npm install

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

# accept terms and conditions
curl -X POST http://admin:admin@localhost:8080/perapi/admin/acceptTermsAndConditions.json

# run the tests
npm run test

# cleanup directory
cd ..
rm -rf peregrine-cms-e2e-testing

# cleanup docker
docker kill peregrine
