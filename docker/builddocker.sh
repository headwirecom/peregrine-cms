#!/bin/bash
if [ "$1" != "skipMaven" ]; then
  cd ..
  mvn clean install
  cd docker
fi
./fetchfiles.sh
docker build --tag=gastongonzalez/peregrine-cms .

