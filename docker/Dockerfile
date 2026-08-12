# Build the (architecture-independent) Sling repository on the native build
# platform so the Sling boot + package install never runs under emulation.
# The Oak segment store, the feature far and the launcher are all portable Java.
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk-jammy AS builder

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
COPY files/org.apache.sling.feature.launcher-1.3.4/ /app/binaries/org.apache.sling.feature.launcher-1.3.4/
COPY scripts/*.sh /app/scripts/

ARG runmode=none
ENV RUNMODE=${runmode}

RUN scripts/install-sling.sh
RUN scripts/install-peregrine.sh ${RUNMODE}



FROM eclipse-temurin:25-jdk-jammy

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
