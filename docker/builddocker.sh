#!/bin/bash
if [ "$1" != "skipMaven" ]; then
  cd ..
  mvn clean install --show-version
  
  if [ $? -ne 0 ]; then
    echo "Exiting build with error due to failed Maven build."
    exit 1
  fi

  cd docker
fi

echo "Removing old build artifacts..."
rm files/*.zip files/*.xz files/*.jar

./fetchfiles.sh
docker build --tag=peregrinecms/peregrine-cms .

