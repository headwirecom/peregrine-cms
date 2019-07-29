#!/bin/bash
if [ "$1" != "skipMaven" ]; then
  cd ..
  mvn clean install --show-version
  cd docker
fi
./fetchfiles.sh
docker build --tag=peregrinecms/peregrine-cms-sling11 .

