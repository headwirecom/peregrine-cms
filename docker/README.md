# Peregrine Docker Image

[![Build Status](https://travis-ci.org/headwirecom/peregrine-cms.svg?branch=develop)](https://travis-ci.org/headwirecom/peregrine-cms)

A prebuilt Peregrine Docker image is available on [Docker Hub](https://hub.docker.com/r/peregrinecms/peregrine-cms).

# Running Peregrine in Docker

1.  Pull down the Docker images.

        $ docker pull peregrinecms/peregrine-cms:latest
        $ docker pull peregrinecms/peregrine-cms:latest-author
        $ docker pull peregrinecms/peregrine-cms:latest-publish

2.  Run the container.

        $ docker run -it -p 8080:8080 peregrinecms/peregrine-cms:latest

3.  Open a browser and visit http://localhost:8080 and login with `admin` / ` admin`.

# Building the Image

If you prefer to build the Peregrine image yourself, simply run:

    $ ./travis-build.sh
    $ cd docker
    $ ./builddocker.sh #local
    $ ./builddocker-remote-replication.sh #author and publish

Launch the locally built docker image

    $ docker run -it -p 8080:8080 peregrinecms/peregrine-cms:latest

or for the remote (author/publish) use-case

    $ docker run -d --rm -p 8080:8080 --name peregrine-author peregrinecms/peregrine-cms:latest-author
    $ docker run -d --rm -p 8180:8080 --name peregrine-publish peregrinecms/peregrine-cms:latest-publish

# Verifying Container Startup

The Peregrine Docker image implements the `HEALTHCHECK` instruction to determine if all
OSGi bundles are ready. If you need to check if the container is ready, use the
`docker inspect` command to retrieve the health status.

1. Find your container ID.

```
$ docker ps
```

2. Inspect the health state object. Replace `<CONTAINER_ID>` with the ID from the ps command above.

```
$ docker inspect --format='{{json .State.Health}}' <CONTAINER_ID>
```

If you have `jq` installed on your machine, you can also run:

```
$ docker inspect <CONTAINER_ID> | jq '.[0].State.Health'
```
