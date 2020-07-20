#!/bin/bash

echo "Giving time for Peregrine to startup..."
sleep 15

# Replicate site to file system
echo "Replicating site content to file system..."
curl -v -u admin:admin -d "withSite=themcleanflex" -X POST 'http://localhost:8080/perapi/admin/tenantSetupReplication.json/content/themecleanflex'

apachectl start
