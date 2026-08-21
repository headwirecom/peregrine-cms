#!/bin/bash
# exit if any command fails
set -e

PACKAGE_DIR=/app/binaries
SLING_URL=http://localhost:8080
# attempts per package before failing the build
INSTALL_ATTEMPTS=3
# seconds to wait between install attempts
INSTALL_RETRY_DELAY=10
# seconds to wait for Sling to shut down cleanly before force-killing
SHUTDOWN_TIMEOUT=120

SAVE_PWD=`pwd`

log() {
  echo "[install-peregrine $(date -u '+%Y-%m-%dT%H:%M:%SZ')] $*"
}

echo "Starting Sling for the first time..."
/app/scripts/start.sh $1

echo "Installing Sling Packager"
npm install @peregrinecms/slingpackager -g

PKG_ORDER=( \
  base.ui.apps-1.0-SNAPSHOT.zip \
  felib.ui.apps-1.0-SNAPSHOT.zip \
  pagerender-vue.ui.apps-1.0-SNAPSHOT.zip \
  pagerender-server.ui.apps-1.0-SNAPSHOT.zip \
  admin.ui.apps-1.0-SNAPSHOT.zip \
  admin.ui.materialize-1.0-SNAPSHOT.zip \
  admin.sling.ui.apps-1.0-SNAPSHOT.zip \
  example-vue.ui.apps-1.0-SNAPSHOT.zip \
  pagerender-vue3.ui.apps-1.0-SNAPSHOT.zip \
  bayviewthemevue3-ui.apps-1.0-SNAPSHOT.zip \
  pagerender-vanilla.ui.apps-1.0-SNAPSHOT.zip \
  bayviewvanilla.ui.apps-1.0-SNAPSHOT.zip \
  postervanilla.ui.apps-1.0-SNAPSHOT.zip \
  napkinvanilla.ui.apps-1.0-SNAPSHOT.zip \
  adminv2.ui.apps-1.0-SNAPSHOT.zip \
)

# Upload a package and echo the package path ("/group/name.zip") on stdout.
upload_package() {
  local pkg=$1
  local out pkg_path

  out=$(slingpackager -v upload ${PACKAGE_DIR}/${pkg})
  echo "${out}" >&2
  # match only the package path from the upload response;
  # the -v debug output also contains "path":"/bin/cpm/package.upload.json"
  pkg_path=$(echo "${out}" | grep -o '"path":"[^"]*\.zip"' | head -1 | cut -d'"' -f4)
  echo "${pkg_path}"
}

# Install an already uploaded package and wait for the result. Composum's
# install servlet executes the install job and only responds once the job has
# finished, so installs are strictly one at a time and the response carries
# the final result ("status":"done" plus the lastUnpacked timestamp).
install_package() {
  local pkg=$1
  local pkg_path=$2
  local resp http status unpacked

  resp=$(curl -u admin:admin -s -w '\n%{http_code}' -X POST "${SLING_URL}/bin/cpm/package.install.json${pkg_path}")
  http=$(echo "${resp}" | tail -1)
  resp=$(echo "${resp}" | sed '$d')
  log "Installer response for '${pkg}' (HTTP ${http}): ${resp}"

  status=$(echo "${resp}" | jq -r '.status // empty' 2>/dev/null || true)
  unpacked=$(echo "${resp}" | jq -r '.package.definition.lastUnpacked // empty' 2>/dev/null || true)
  if [ "${http}" = "200" ] && [ "${status}" = "done" ] && [ -n "${unpacked}" ]; then
    log "OK: '${pkg}' installed, lastUnpacked=${unpacked}"
    return 0
  fi

  log "ERROR: install of '${pkg}' did not complete successfully"
  log "Recent errors from the Sling log:"
  grep "\*ERROR\*" /app/sling/logs/error.log | grep -v "GraphiQLRequestFilter" | tail -10 || true
  return 1
}

for pkg in "${PKG_ORDER[@]}"
do
  log "Uploading package '${pkg}'..."
  pkg_path=$(upload_package "${pkg}")
  if [ -z "${pkg_path}" ]; then
    log "FATAL: upload of '${pkg}' failed - aborting image build"
    exit 1
  fi

  installed=false
  for attempt in $(seq 1 ${INSTALL_ATTEMPTS}); do
    log "Installing '${pkg}' (attempt ${attempt} of ${INSTALL_ATTEMPTS})..."
    if install_package "${pkg}" "${pkg_path}"; then
      installed=true
      break
    fi
    if [ ${attempt} -lt ${INSTALL_ATTEMPTS} ]; then
      log "Waiting ${INSTALL_RETRY_DELAY}s before retrying '${pkg}'..."
      sleep ${INSTALL_RETRY_DELAY}
    fi
  done
  if [ "${installed}" != "true" ]; then
    log "FATAL: package '${pkg}' failed to install after ${INSTALL_ATTEMPTS} attempts - aborting image build"
    exit 1
  fi
done

log "All ${#PKG_ORDER[@]} packages installed and verified."

# ---------------------------------------------------------------------------
# Remove the classic (v1) admin UI. Admin v2 is the only console in this image.
# We KEEP the shared backend and felib assets v2 depends on:
#   /apps/admin/install  - the admin.core bundle that serves /perapi
#   /etc/felibs/admin    - icon-browser fonts + i18n dictionaries v2 loads
# and delete only the Vue 2 UI content:
#   /content/admin, /apps/admin/components, /apps/admin/pages, /apps/field
# ---------------------------------------------------------------------------
log "Removing the classic (v1) admin UI (keeping admin.core + felibs/admin)..."
for classic in /content/admin /apps/admin/components /apps/admin/pages /apps/field; do
  code=$(curl -u admin:admin -s -o /dev/null -w '%{http_code}' -X POST "${SLING_URL}${classic}" -F ":operation=delete")
  log "  deleted ${classic} (HTTP ${code})"
done

# Wait for Sling to be fully ready again
# (package installs may have triggered bundle restarts / sling jobs)
QUIESCE_TIMEOUT=300
waited=0
while [ "$(curl -u admin:admin -s --fail  ${SLING_URL}/system/console/bundles.json | jq '.s[3:5]' -c)" != "[0,0]" ]
do
  if [ ${waited} -ge ${QUIESCE_TIMEOUT} ]; then
    log "FATAL: bundles still not all active after ${QUIESCE_TIMEOUT}s - aborting image build"
    log "Bundles not in Active/Fragment state:"
    curl -u admin:admin -s "${SLING_URL}/system/console/bundles.json" | jq -r '.data[] | select(.state != "Active" and .state != "Fragment") | "\(.state)  \(.symbolicName)  \(.version)"'
    log "Recent errors from the Sling log:"
    grep "\*ERROR\*" /app/sling/logs/error.log | grep -v "GraphiQLRequestFilter" | tail -15 || true
    exit 1
  fi
  log "Sling still starting. Waiting for all bundles to be ready.."
  sleep 5
  waited=$((waited+5))
done

SLING_PID=`ps -ef | grep org.apache.sling.feature.launcher | grep -v grep | awk '{print $2}'`
log "Stopping Sling (pid ${SLING_PID})..."
kill ${SLING_PID}

# Wait for the JVM to actually exit; killing the image build step while Oak is
# still flushing the segment store can corrupt or lose repository state.
waited=0
while kill -0 ${SLING_PID} 2>/dev/null; do
  if [ ${waited} -ge ${SHUTDOWN_TIMEOUT} ]; then
    log "WARNING: Sling did not stop within ${SHUTDOWN_TIMEOUT}s, force killing"
    kill -9 ${SLING_PID}
    break
  fi
  sleep 2
  waited=$((waited+2))
done
log "Sling stopped."

cd ${SAVE_PWD}
