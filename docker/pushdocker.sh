#!/bin/bash

. env.sh

imageid=$(docker images ${DOCKER_IMAGE} --format {{.ID}})
docker tag $imageid ${DOCKER_IMAGE}
docker push peregrinecms/peregrine-cms
