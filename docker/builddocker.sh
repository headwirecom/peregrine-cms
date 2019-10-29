#!/bin/bash

usage () {
  echo "Usage: `basename $0` <build type>"
  echo "  <build type>   build|deploy)"
}

case $1 in
  build)
    if [ "$2" != "skipMaven" ]; then
      cd ..
      mvn clean install --show-version
  
      if [ $? -ne 0 ]; then
        echo "Exiting build with error due to failed Maven build."
        exit 1
      fi

      cd docker
    fi
  ;;
  deploy)
    if [ "$2" != "skipMaven" ]; then
      cd ..
      gpg --version
      mvn clean deploy -Peregrine-release --show-version --settings settings.xml
  
      if [ $? -ne 0 ]; then
        echo "Exiting build with error due to failed Maven build."
        exit 1
      fi

      cd docker
    fi
  ;;
  *)
    echo "Unsupported build specification: $1"
    usage
    exit 1
esac

echo "Removing old build artifacts..."
rm files/*.zip files/*.xz files/*.jar

./fetchfiles.sh
docker build --tag=peregrinecms/peregrine-cms:sling11 .

