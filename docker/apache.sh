#!/bin/bash

sleep 15

# Replicate site to file system
curl -v -u admin:admin -d "withSite=themcleanflex" -X POST 'http://localhost:8080/perapi/admin/tenantSetupReplication.json/content/themecleanflex'

apachectl -D FOREGROUND
