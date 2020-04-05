#!/bin/bash

imageid=$(docker images peregrinecms/peregrine-cms:issues-93-20200405r1 --format {{.ID}})
docker tag $imageid peregrinecms/peregrine-cms:issues-93-20200405r1
docker push peregrinecms/peregrine-cms
