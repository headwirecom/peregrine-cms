# Peregrine-CMS and Docker

> **_NOTE:_** The provided docker instances of Peregrine-CMS are for testing purposes. The contents in the docker instance is lost
when the docker instance is terminated.

> **_NOTE:_** Docker on Windows is known to have file system performance issues. Please install Docker Desktop and the Linux Subsystem
on your windows computer - once installed, make sure you run the Peregrine-CMS Docker instance from your Linux Subsystem in a
Linux path (not the mounted folders from Windows).

## Run Peregrine-CMS, Develop-Sling12 Branch

```bash
docker run -it -p 8080:8080 peregrinecms/peregrinecms:develop-sling12
```

## Pull the latest version of the docker image

```bash
docker pull peregrinecms/peregrinecms:develop-sling12
```

## Check out a PR with docker

replace the {number} in the command below with the PR number for the change you would like to inspect

```bash
docker run -it -p 8080:8080 peregrinecms/peregrinecms:pr-{number}
```

## Run Peregrine-CMS with a Webserver

```bash
docker run -it -p 8080:8080 -p 8000:8000 --export 8000 --name peregrine peregrinecms/peregrinecms:develop-sling12
docker exec -it peregrine bash
cd /apps/sling/staticreplication
npx httpserver -p 8000
```

Use your browser to go to the website (<http://localhost:8000>) or Peregrine-CMS (<http://localhost:8080>)
