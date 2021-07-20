# Peregrine-CMS and Docker

The fastest way to get various versions of Peregrine-CMS running locally is to use the provided docker images. Install docker desktop locally to set up your computer with docker support.
Docker desktop is supported on Linux, Mac OS-X and on Windows Professional.

> **_NOTE:_** The provided docker instances of Peregrine-CMS are for testing purposes. The contents in the docker instance is lost
> when the docker instance is terminated.

> **_NOTE:_** Docker on Windows is known to have file system performance issues. Please install Docker Desktop and the Linux Subsystem
> on your windows computer - once installed, make sure you run the Peregrine-CMS Docker instance from your Linux Subsystem in a
> Linux path (not the mounted folders from Windows).

## Run Peregrine-CMS, Develop-Sling12 Branch

```bash
docker run -it -p 8080:8080 peregrinecms/peregrine-cms:develop-sling12
```

this command runs a docker container

-   `-it`: log output to stdout in the container is forwarded to your console and console blocks until you hit `ctrl-c` to stop the container
-   `-p 8080:8080`: Your local port 8080 is forwarded to the container port 8080

Use your browser to go to Peregrine-CMS (<http://localhost:8080>)

## Fetch an updated version of the docker image

```bash
docker pull peregrinecms/peregrine-cms:develop-sling12
```

-   `pull`: checks and pulls a new version from docker hub for the docker image

## Check out a PR with docker

Replace the `{number}` in the command below with the PR number for the change you would like to inspect

```bash
docker run -it -p 8080:8080 peregrinecms/peregrine-cms:pr-{number}
```

> if you want to run multiple instances of Peregrine-CMS at the same time you can do so by changing the first number of `-p 8080:8080` to another TCP/IP Port. Try `docker run -it -p 8812:8080 peregrinecms/peregrine-cms:pr-812` and then access the instance with your browser at (<http://localhost:8812>)

## Run Peregrine-CMS and start a Webserver in the container

```bash
docker run -rm -it -p 8080:8080 -p 8000:8000 --name peregrine peregrinecms/peregrine-cms:develop-sling12
```

-   `--name peregrine`: we give our container a name (use `docker ps` to see all your running docker containers)
-   `-rm`: the additional `-rm` removes the docker container when we stop it and frees the name of the container
-   `-p 8000:8000`: in addition to port 8080, also forward port 8000 to the docker container

in a second shell/command line run

```bash
docker exec -it peregrine bash
```

in the docker container `peregrine` **exec**ute `bash`

once in the shell in the container run the following commands:

```bash
cd /apps/sling/staticreplication
npx httpserver -p 8000
```

We change the shell to the location where peregrine stores the html files at replication and then start a nodejs based webserver at that location.

Use your browser to go to the website (<http://localhost:8000>) or Peregrine-CMS (<http://localhost:8080>)

## Run Peregrine-CMS with an author and publish (stage and live) instance

```bash
docker run -d --rm --network=host --name peregrine-author peregrinecms/peregrine-cms-author:develop-sling12
docker run -d --rm -p 8180:8080 --name peregrine-publish peregrinecms/peregrine-cms-publish:develop-sling12
```

> `--network=host` makes the author docker instance use the host
> network interface. By default, remote replication in peregrine
> expects a publisher to be available on port 8180.

to stop the instances use

```bash
docker kill peregrine-author peregrine-publish
```
