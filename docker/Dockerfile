FROM adoptopenjdk/openjdk11:latest AS builder

WORKDIR /app

RUN apt update -q \
    && apt upgrade -q -y \
    && apt install -q -y --no-install-recommends \
        curl \
        jq \
        nodejs \
        npm \
    && apt clean -q -y \
    && apt autoremove --purge -q -y \
    && echo node: $(node --version) \
    && echo npm: $(npm --version) \
    && echo npx: $(npx --version)

COPY files/* /app/binaries/
COPY scripts/*.sh /app/scripts/

ARG runmode=none
ENV RUNMODE=${runmode}

RUN scripts/install-sling.sh
RUN scripts/install-peregrine.sh ${RUNMODE}



FROM adoptopenjdk/openjdk11:latest

WORKDIR /app

COPY --from=builder /app/scripts ./scripts
COPY --from=builder /app/sling ./sling

RUN apt update -q \
    && apt install -q -y --no-install-recommends \
        jq \
        curl \
        libvips-tools \
    && apt clean -q -y \
    && apt autoremove --purge -q -y

ARG runmode=none
ENV RUNMODE=${runmode}

ENTRYPOINT /app/scripts/start.sh ${RUNMODE} && tail -qF /app/sling/logs/error.log

#HEALTHCHECK --interval=15s --retries=20 \
#  CMD /app/healthcheck.sh

EXPOSE 8080
