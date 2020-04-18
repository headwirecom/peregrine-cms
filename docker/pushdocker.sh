#!/bin/bash

imageid=$(docker images peregrinecms/peregrine-cms:issues-93-20200418r1 --format {{.ID}})
docker tag $imageid peregrinecms/peregrine-cms:issues-93-20200418r1
docker push peregrinecms/peregrine-cms
