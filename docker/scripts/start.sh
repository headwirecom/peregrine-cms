#!/bin/bash

echo starting sling with runmode $1

cd /app/sling && java -jar /app/sling/org.apache.sling.feature.launcher.jar \
    -D sling.run.modes=$1 \
    -f /app/sling/com.peregrine-cms.sling.launchpad-12-SNAPSHOT-oak_tar_far.far \
    -p /app/sling \
    -c /app/sling/launcher/cache &

# Wait for Sling to fully start up
while [ "$(curl -u admin:admin -s --fail  http://localhost:8080/system/console/bundles.json | jq '.s[3:5]' -c)" != "[0,0]" ]
do
  echo "Sling still starting. Waiting for all bundles to be ready.."
  sleep 2
done
