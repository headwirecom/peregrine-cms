FROM adoptopenjdk/openjdk11:latest

WORKDIR /app

RUN apt update -q \
    && apt upgrade -q -y \
    && apt install -q -y --no-install-recommends \
        curl \
        jq \
        libvips libvips-tools \
        tar \
        xz-utils

ENV PATH "/usr/local/lib/nodejs/node-v10.16.0-linux-x64/bin:$PATH"


# Start - Use repository/package pinning to upgrade libvips from version 8.4 to 8.6
COPY overlay/etc/apt/preferences.d/* /etc/apt/preferences.d/
RUN echo "deb http://archive.ubuntu.com/ubuntu/ focal main restricted" >> /etc/apt/sources.list \
    && echo "deb http://archive.ubuntu.com/ubuntu/ focal universe" >> /etc/apt/sources.list \
    && apt update \
    && apt --only-upgrade install -y libvips libvips-tools
# End 

COPY files/node-v10.16.0-linux-x64.tar.xz /app/binaries/node-v10.16.0-linux-x64.tar.xz

RUN mkdir -p /usr/local/lib/nodejs \
    && tar -xf /app/binaries/node-v10.16.0-linux-x64.tar.xz -C /usr/local/lib/nodejs \
    && echo $PATH \
    && echo node: $(node -v) \
    && echo npm: $(npm -v) \
    && echo npx: $(npx -v) \
    && apt-get purge -y --auto-remove && rm -rf /var/lib/apt/lists/* && rm -rf /etc/apt/sources.list.d/temp.list;

ARG runmode=none
ENV APP_VERSION 2.0.0
ENV RUNMODE=${runmode}

COPY files/* /app/binaries/
COPY scripts/*.sh /app/scripts/

RUN scripts/install-sling.sh
RUN scripts/install-peregrine.sh ${RUNMODE}

ENTRYPOINT /app/scripts/start.sh ${RUNMODE} && tail -qF /app/sling/logs/error.log

#HEALTHCHECK --interval=15s --retries=20 \
#  CMD /app/healthcheck.sh

EXPOSE 8080
