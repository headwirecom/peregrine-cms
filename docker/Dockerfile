FROM adoptopenjdk/openjdk8:latest

ARG PEREGRINECMS_BRANCH=develop
ARG THEMECLEANFLEX_BRANCH=develop

ENV APP_VERSION 1.0.0-snapshot

WORKDIR /app

RUN apt update -q \
    && apt upgrade -q -y \
    && apt install -q -y --no-install-recommends \
        curl \
        jq \
        libvips libvips-tools \
        tar \
        xz-utils \
        git \
        maven

ENV PATH "/usr/local/lib/nodejs/node-v10.16.0-linux-x64/bin:$PATH"

COPY files/* /app/binaries/
COPY healthcheck.sh .
COPY installpackages.sh .
COPY patchfiles/tasks.js .
COPY apache.sh .

# Start - Use repository/package pinning to upgrade libvips from version 8.4 to 8.6
COPY peregrine.pref /etc/apt/preferences.d/
RUN echo "deb http://archive.ubuntu.com/ubuntu/ eoan main restricted" >> /etc/apt/sources.list \
    && echo "deb http://archive.ubuntu.com/ubuntu/ eoan universe" >> /etc/apt/sources.list \
    && apt update \
    && apt --only-upgrade install -y libvips libvips-tools
# End 

RUN mkdir -p /usr/local/lib/nodejs \
    && tar -xf /app/binaries/node-v10.16.0-linux-x64.tar.xz -C /usr/local/lib/nodejs \
    && echo "peregrine-cms branch: $PEREGRINECMS_BRANCH" \
    && echo "themeclean-flex branch: $THEMECLEANFLEX_BRANCH" \
    && java -version \
    && echo $PATH \
    && echo node: $(node -v) \
    && echo npm: $(npm -v) \
    && echo npx: $(npx -v) \
    && apt-get purge -y --auto-remove && rm -rf /var/lib/apt/lists/* && rm -rf /etc/apt/sources.list.d/temp.list;

RUN npm install percli @peregrinecms/slingpackager \
    && npx percli \
    && npx percli check java \
    && npx @peregrinecms/slingpackager --version \
    && mkdir out \
    && cp binaries/*.jar out \
    && chmod a+x node_modules/percli/bin/*
RUN mv /app/tasks.js /app/node_modules/percli/lib/tasks.js
RUN npx percli server register author
RUN npx percli server start author \
    && ./installpackages.sh $PEREGRINECMS_BRANCH $THEMECLEANFLEX_BRANCH \
    && sleep 10 \
    && npx percli server stop author \
    && rm -rf binaries


# Add Apache HTTP server
RUN apt update -q  && apt-get install -q -y apache2
COPY apache2/ /etc/apache2/
RUN curl -L -O https://dl-ssl.google.com/dl/linux/direct/mod-pagespeed-beta_current_amd64.deb \
    && echo "N" | dpkg -i mod-pagespeed-*.deb \
    && a2enmod expires \
    && a2enmod macro \
    && a2enmod pagespeed \
    && a2enmod proxy_http \
    && a2enmod rewrite \
    && a2dissite 000-default \
    && a2ensite peregrine

ENTRYPOINT npx percli server start && /app/apache.sh && tail -qF sling/logs/*

HEALTHCHECK --interval=15s --retries=20 \
  CMD /app/healthcheck.sh

EXPOSE 8080 80
