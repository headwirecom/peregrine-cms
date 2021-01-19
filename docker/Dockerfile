FROM adoptopenjdk/openjdk11:latest

ARG PEREGRINECMS_REPO=https://github.com/headwirecom/peregrine-cms.git
ARG PEREGRINECMS_BRANCH=develop-sling12
ARG THEMECLEANFLEX_REPO=https://github.com/headwirecom/themeclean-flex.git
ARG THEMECLEANFLEX_BRANCH=develop
ENV APP_VERSION 2.0.0

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
COPY scripts/*.sh /app/scripts/

RUN mkdir -p /root/.m2
COPY overlay/root/.m2/settings.xml /root/.m2/

# Start - Use repository/package pinning to upgrade libvips from version 8.4 to 8.6
COPY overlay/etc/apt/preferences.d/* /etc/apt/preferences.d/
RUN echo "deb http://archive.ubuntu.com/ubuntu/ focal main restricted" >> /etc/apt/sources.list \
    && echo "deb http://archive.ubuntu.com/ubuntu/ focal universe" >> /etc/apt/sources.list \
    && apt update \
    && apt --only-upgrade install -y libvips libvips-tools
# End 

RUN mkdir -p /usr/local/lib/nodejs \
    && tar -xf /app/binaries/node-v10.16.0-linux-x64.tar.xz -C /usr/local/lib/nodejs \
    && echo $PATH \
    && echo node: $(node -v) \
    && echo npm: $(npm -v) \
    && echo npx: $(npx -v) \
    && apt-get purge -y --auto-remove && rm -rf /var/lib/apt/lists/* && rm -rf /etc/apt/sources.list.d/temp.list;

RUN scripts/checkout-projects.sh $PEREGRINECMS_REPO $PEREGRINECMS_BRANCH \
    $THEMECLEANFLEX_REPO $THEMECLEANFLEX_BRANCH
RUN scripts/install-sling.sh
RUN scripts/install-peregrine.sh

ENTRYPOINT /app/scripts/start.sh && tail -qF /app/sling/logs/error.log

#HEALTHCHECK --interval=15s --retries=20 \
#  CMD /app/healthcheck.sh

EXPOSE 8080
