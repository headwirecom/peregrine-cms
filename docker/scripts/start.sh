#!/bin/bash

cd /app/sling && java -jar /app/sling/org.apache.sling.feature.launcher.jar \
    -f /app/sling/feature-oak_tar.json \
    -p /app/sling \
    -c /app/sling/launcher/cache &
