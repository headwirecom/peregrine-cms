#!/bin/bash

echo "Removing old build artifacts..."
rm files/*.xz files/*.jar

./fetchfiles.sh
docker build --tag=peregrinecms/peregrine-cms:issues-93-20200419r1 .

