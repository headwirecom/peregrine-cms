#!/bin/bash
if [ "$1" != "skipMaven" ]; then
  cd ..
  mvn clean install --show-version
  cd docker
fi

echo "Removing old build artifacts..."
rm files/*.zip files/*.xz files/*.jar

./fetchfiles.sh
docker build --tag=peregrinecms/peregrine-cms .

