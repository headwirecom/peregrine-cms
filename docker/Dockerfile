FROM adoptopenjdk/openjdk8:latest

WORKDIR /app

RUN apt update -q && apt upgrade -q -y && apt install -q -y --no-install-recommends tar curl xz-utils jq

ENV PATH "/usr/local/lib/nodejs/node-v10.15.3-linux-x64/bin:$PATH"

COPY files/* /app/binaries/
COPY installpackage.sh .

RUN mkdir -p /usr/local/lib/nodejs \
    && tar -xf /app/binaries/node-v10.15.3-linux-x64.tar.xz -C /usr/local/lib/nodejs \
    && java -version \
    && echo $PATH \
    && node -v \
    && npm -v \
    && npx -v \
    && find .

RUN npm install percli
RUN npx percli
RUN mkdir out
RUN cp binaries/*.jar out
RUN chmod a+x node_modules/percli/bin/*
RUN npx percli server register author
RUN npx percli server start author \
    && bash installpackage.sh "http://admin:admin@localhost:8080" binaries/base.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackage.sh "http://admin:admin@localhost:8080" binaries/external-1.0-SNAPSHOT.zip \
    && bash installpackage.sh "http://admin:admin@localhost:8080" binaries/felib.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackage.sh "http://admin:admin@localhost:8080" binaries/pagerender-vue.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackage.sh "http://admin:admin@localhost:8080" binaries/admin.sling.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackage.sh "http://admin:admin@localhost:8080" binaries/admin.ui.materialize-1.0-SNAPSHOT.zip \
    && bash installpackage.sh "http://admin:admin@localhost:8080" binaries/admin.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackage.sh "http://admin:admin@localhost:8080" binaries/themeclean-ui.apps-1.0-SNAPSHOT.zip 
RUN npx percli server stop author

ENTRYPOINT npx percli server start && tail -qF sling/logs/*

EXPOSE 8080