#!/bin/bash

imageid=$(docker images peregrine-cms:latest --format {{.ID}})
docker tag $imageid peregrinecms/peregrine-cms:develop
docker push peregrinecms/peregrine-cms
