#!/bin/bash

imageid=$(docker images peregrine-cms:latest --format {{.ID}})
docker tag $imageid reusr1/peregrine-cms:develop
docker push reusr1/peregrine-cms
