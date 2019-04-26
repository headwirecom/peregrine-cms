#!/bin/bash

mvn clean install
cd docker
./fetchfiles.sh
docker build --tag=peregrine-cms .
cd ..
