#!/bin/bash

cd /app/sling && java -jar /app/sling/org.apache.sling.feature.launcher.jar \
    -f /app/sling/feature-oak_tar.json \
    -p /app/sling  \
    -c /app/sling/launcher/cache &

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