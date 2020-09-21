#!/bin/bash

usage () {
  echo "Usage: `basename $0` <peregrine repo> <peregrine branch>"
}

if [ $# -ne 2 ]; then
  usage
  exit 1
fi

REPO=$1
BRANCH=$2

echo "Checking out ${REPO} and using ${BRANCH}..."

SAVE_PWD=`pwd`

git clone ${REPO}
cd peregrine-cms
git checkout ${PEREGRINECMS_BRANCH}

cd ${SAVE_PWD}
