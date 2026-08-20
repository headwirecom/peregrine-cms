#!/bin/bash

echo starting sling with runmode $1

# Required on Java 17+ for ThreadLocal cleanup (see Sling 12 release notes)
export JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED ${JAVA_OPTS}"

# The felix.http proxy flag makes jetty honour X-Forwarded-Proto/-Port: behind
# an https tunnel or proxy, redirects (e.g. anonymous -> login form) otherwise
# carry absolute http:// Locations, which browsers kill as mixed content.
cd /app/sling && /app/sling/org.apache.sling.feature.launcher-*/bin/launcher \
    -D sling.run.modes=$1 \
    -D org.apache.felix.proxy.load.balancer.connection.enable=true \
    -f /app/sling/com.peregrine-cms.sling.launchpad-*-SNAPSHOT-oak_tar_far.far \
    -p /app/sling \
    -c /app/sling/launcher/cache &

# Wait for Sling to fully start up. On a repository whose admin password has
# been changed (a mounted volume), admin:admin answers 401 forever - but a 401
# means authentication is up, which is as ready as this loop can observe, so
# accept it instead of spinning for the container's lifetime.
while :
do
  code=$(curl -u admin:admin -s -o /tmp/bundles.json -w '%{http_code}' http://localhost:8080/system/console/bundles.json)
  if [ "$code" = "401" ]; then
    echo "Sling is up (custom admin password - skipping the bundle check)."
    break
  fi
  if [ "$code" = "200" ] && [ "$(jq '.s[3:5]' -c < /tmp/bundles.json)" = "[0,0]" ]; then
    break
  fi
  echo "Sling still starting. Waiting for all bundles to be ready.."
  sleep 2
done
