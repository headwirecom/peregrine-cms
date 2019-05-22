# Peregrine Docker Image

A prebuilt Peregrine Docker image is available on [Docker Hub](https://cloud.docker.com/repository/docker/reusr1/peregrine-cms).


# Running Peregrine in Docker

1. Pull down the Docker image.

        $ docker pull reusr1/peregrine-cms:develop

2. Run the container.

        $ docker run -it --rm -p 8080:8080 reusr1/peregrine-cms:develop

3. Open a browser and visit http://localhost:8080. Login with `admin` / ` admin`.


# Building the Image

If you prefer to build the Peregrine image yourself, simply run:

    $ ./builddocker.sh
