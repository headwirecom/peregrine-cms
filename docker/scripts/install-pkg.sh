#!/bin/bash
#
# Example Usage: ./install-pkg.sh /com.peregrine-cms/admin.sling.ui.apps-1.0-SNAPSHOT.zip
# 
# exit if any command fails
set -e

USER=admin
PASS=admin
HOST=localhost
PORT=8080
OUT_FILE=sling-event-job.json

which jq
if [ $? -ne 0 ]; then
  echo "This utility requires 'jq' to run. Please install 'jq' first."
  exit 1
fi

usage () {
  echo "Usage: `basename $0` <package>"
}

if [ $# -ne 1 ]; then
  usage
  exit 1
fi

PKG=$1
#PKG=/com.peregrine-cms/admin.sling.ui.apps-1.0-SNAPSHOT.zip

# Request package install
curl -u ${USER}:${PASS} \
	-v \
	-f \
	-X POST \
	-o ${OUT_FILE} \
        -F "operation=install" \
        -F "event.job.topic=com/composum/sling/core/pckgmgr/PackageJobExecutor" \
        -F "reference=${PKG}" \
	http://${HOST}:${PORT}/bin/cpm/core/jobcontrol.job.json


SLINGEVENT_EVENT_ID=`cat ${OUT_FILE} | jq '."slingevent:eventId"' | sed 's/"//g'`
echo "Polling Sling Event ID: '${SLINGEVENT_EVENT_ID}'"

STATUS="PENDING"
while [ $STATUS != "SUCCEEDED" ]
do
  echo "Waiting for Sling job to complete"
  sleep 5 
  curl -u ${USER}:${PASS} \
    -v \
    -X GET \
    -o ${OUT_FILE} \
    -d "topic=com/composum/sling/core/pckgmgr/PackageJobExecutor" \
    http://${HOST}:${PORT}/bin/cpm/core/jobcontrol.job.json/${SLINGEVENT_EVENT_ID}

  STATUS=`cat ${OUT_FILE} | jq '.jobState' | sed 's/"//g'`
  echo "Job status: '${STATUS}'"
done

rm ${OUT_FILE}
