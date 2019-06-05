FROM adoptopenjdk/openjdk8:latest

ENV APP_VERSION 1.0.0-snapshot

WORKDIR /app

RUN apt update -q \
    && apt upgrade -q -y \
    && apt install -q -y --no-install-recommends \
        curl \
        jq \
        tar \
        xz-utils

ENV PATH "/usr/local/lib/nodejs/node-v10.15.3-linux-x64/bin:$PATH"

COPY files/* /app/binaries/
COPY healthcheck.sh .
COPY installpackage.sh .
COPY installpackageretry.sh .

RUN mkdir -p /usr/local/lib/nodejs \
    && tar -xf /app/binaries/node-v10.15.3-linux-x64.tar.xz -C /usr/local/lib/nodejs \
    && java -version \
    && echo $PATH \
    && echo node: $(node -v) \
    && echo npm: $(npm -v) \
    && echo npx: $(npx -v) \
    && apt-get purge -y --auto-remove && rm -rf /var/lib/apt/lists/* && rm -rf /etc/apt/sources.list.d/temp.list;

RUN npm install percli \
    && npx percli \
    && mkdir out \
    && cp binaries/*.jar out \
    && chmod a+x node_modules/percli/bin/*
RUN npx percli server register author
RUN npx percli server start author \
    && bash installpackageretry.sh "http://admin:admin@localhost:8080" binaries/base.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackageretry.sh "http://admin:admin@localhost:8080" binaries/external-1.0-SNAPSHOT.zip \
    && bash installpackageretry.sh "http://admin:admin@localhost:8080" binaries/felib.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackageretry.sh "http://admin:admin@localhost:8080" binaries/pagerender-vue.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackageretry.sh "http://admin:admin@localhost:8080" binaries/admin.sling.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackageretry.sh "http://admin:admin@localhost:8080" binaries/admin.ui.materialize-1.0-SNAPSHOT.zip \
    && bash installpackageretry.sh "http://admin:admin@localhost:8080" binaries/admin.ui.apps-1.0-SNAPSHOT.zip \
    && bash installpackageretry.sh "http://admin:admin@localhost:8080" binaries/themeclean-ui.apps-1.0-SNAPSHOT.zip \
    && sleep 10 \
    && npx percli server stop author \
    && rm -rf binaries

ENTRYPOINT npx percli server start && tail -qF sling/logs/*

HEALTHCHECK --interval=15s --retries=20 \
  CMD /app/healthcheck.sh

EXPOSE 8080
