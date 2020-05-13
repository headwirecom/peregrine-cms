#!/bin/bash

imageid=$(docker images peregrinecms/peregrine-cms:sso-20200513r1 --format {{.ID}})
docker tag $imageid peregrinecms/peregrine-cms:sso-20200513r1
docker push peregrinecms/peregrine-cms
