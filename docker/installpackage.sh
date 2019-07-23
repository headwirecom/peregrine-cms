#!/bin/bash

### this file is retired and left here for now just because. you can use https://www.npmjs.com/package/@peregrinecms/slingpackager
maxretry=10
echo 
echo "installing package $2"
echo "check if sling is up and running, using a max of $maxretry retries"
sleep 10

status=$(curl -s $1/system/console/bundles.json | jq ".s[3:5]" -c)
retry=0
while [ "$status" != "[0,0]" ]
do
  let retry=retry+1
  echo retry $retry status: $(curl -s $1/system/console/bundles.json | jq ".s" -c)
  if [ $retry -eq $maxretry ]; then
    echo "aborting package install due to server status"
    exit 1
  fi
  sleep 1
  status=$(curl -s http://admin:admin@localhost:9080/system/console/bundles.json | jq ".s[3:5]" -c)
done

curl -f -s -F file=@"$2" -F name="package" -F force=true -F install=true "$1/bin/cpm/package.service.html"

sleep 10
echo "waiting for system to be fully up and running"
status=$(curl -s $1/system/console/bundles.json | jq ".s[3:5]" -c)
retry=0
while [ "$status" != "[0,0]" ]
do
  let retry=retry+1
  echo retry $retry status: $(curl -s $1/system/console/bundles.json | jq ".s" -c)
  if [ $retry -eq $maxretry ]; then
    echo "aborting due to server status"
    exit 1
  fi
  sleep 1
  status=$(curl -s http://admin:admin@localhost:9080/system/console/bundles.json | jq ".s[3:5]" -c)
done
