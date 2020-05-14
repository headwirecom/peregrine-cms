#!/bin/bash

. env.sh

imageid=$(docker images ${DOCKER_IMAGE} --format {{.ID}})
echo $imageid
exit 1
docker tag $imageid peregrinecms/peregrine-cms:sso-20200513r1
docker push peregrinecms/peregrine-cms
