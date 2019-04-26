#!/bin/bash
maxretry=10
echo 
echo "installing package $2"
echo "check if sling is up and running, using a max of $maxretry"
sleep 5

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

curl -s -F file=@"$2" -F name="package" -F force=true -F install=true "$1/bin/cpm/package.service.html"