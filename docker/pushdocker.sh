#!/bin/bash

imageid=$(docker images peregrinecms/peregrine-cms:sso-develop-20200319r1 --format {{.ID}})
docker tag $imageid peregrinecms/peregrine-cms:sso-develop-20200319r1
docker push peregrinecms/peregrine-cms
