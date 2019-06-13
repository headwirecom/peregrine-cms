#!/bin/bash

maxretry=10
retry=0
./installpackage.sh $1 $2
status=$?
while [ "$status" != "0" ]
do
  let retry=retry+1
  echo retry $retry status: $status
  if [ $retry -eq $maxretry ]; then
    echo "aborting package install due to package install failure"
    exit 1
  fi
  sleep 5
  ./installpackage.sh $1 $2  
  status=$?
done

