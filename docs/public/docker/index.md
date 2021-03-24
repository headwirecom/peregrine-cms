# Peregrine-CMS and Docker

The fastest way to get various versions of Peregrine-CMS running locally is to use the provided docker images. Install docker desktop locally to set up your computer with docker support.
Docker desktop is supported on Linux, Mac OS-X and on Windows Professional.

> **_NOTE:_** The provided docker instances of Peregrine-CMS are for testing purposes. The contents in the docker instance is lost
when the docker instance is terminated.

> **_NOTE:_** Docker on Windows is known to have file system performance issues. Please install Docker Desktop and the Linux Subsystem
on your windows computer - once installed, make sure you run the Peregrine-CMS Docker instance from your Linux Subsystem in a
Linux path (not the mounted folders from Windows).

## Run Peregrine-CMS, Develop-Sling12 Branch

```bash
docker run -it -p 8080:8080 peregrinecms/peregrine-cms:develop-sling12
```
this command runs a docker container
- `-it`: log output to stdout in the container is forwarded to your console and console blocks until you hit `ctrl-c` to stop the container
- `-p 8080:8080`: Your local port 8080 is forwarded to the container port 8080

Use your browser to go to Peregrine-CMS (<http://localhost:8080>)

## Fetch an updated version of the docker image

```bash
docker pull peregrinecms/peregrine-cms:develop-sling12
```
- `pull`: checks and pulls a new version from docker hub for the docker image

## Check out a PR with docker

Replace the `{number}` in the command below with the PR number for the change you would like to inspect

```bash
docker run -it -p 8080:8080 peregrinecms/peregrine-cms:pr-{number}
```

## Run Peregrine-CMS and start a Webserver in the container

```bash
docker run -rm -it -p 8080:8080 -p 8000:8000 --name peregrine peregrinecms/peregrine-cms:develop-sling12
```
- `--name peregrine`: we give our container a name (use `docker ps` to see all your running docker containers)
- ` -rm`: the additional `-rm` removes the docker container when we stop it and frees the name of the container
- `-p 8000:8000`: in addition to port 8080, also forward port 8000 to the docker container

in a second shell/command line run
```bash
docker exec -it peregrine bash
```
in the docker container `peregrine` **exec**ute `bash` 

once in the shell in the container run the following commands:
```
cd /apps/sling/staticreplication
npx httpserver -p 8000
```
We change the shell to the location where peregrine stores the html files at replication and then start a nodejs based webserver at that location. 


Use your browser to go to the website (<http://localhost:8000>) or Peregrine-CMS (<http://localhost:8080>)
