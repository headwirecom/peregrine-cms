#!/bin/bash
if [ "$1" != "skipMaven" ]; then
  cd ..
  mvn clean install
  cd docker
fi
./fetchfiles.sh
docker build --tag=peregrinecms/peregrine-cms .

