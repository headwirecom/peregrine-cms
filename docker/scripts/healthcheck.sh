#!/bin/bash

STATUS_HEALTHY=0
STATUS_UNHEALTHY=1

status=$(curl -u admin:admin -s --fail  http://localhost:8080/system/console/bundles.json | jq '.s[3:5]' -c)
if [ "$status" != "[0,0]" ]; then
  echo "Bundles not ready."
  exit $STATUS_UNHEALTHY
else
  echo "Bundles ready."
  exit $STATUS_HEALTH
fi
