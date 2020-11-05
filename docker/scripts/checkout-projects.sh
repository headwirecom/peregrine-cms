#!/bin/bash

usage () {
  echo "Usage: `basename $0` <peregrine repo> <peregrine branch> <themeclean-flex repo> <themeclean-flex branch>"
}

if [ $# -ne 4 ]; then
  usage
  exit 1
fi

PEREGRINECMS_REPO=$1
PEREGRINECMS_BRANCH=$2
THEMECLEANFLEX_REPO=$3
THEMECLEANFLEX_BRANCH=$4

SAVE_PWD=`pwd`

echo "Checking out ${PEREGRINECMS_REPO} and using ${PEREGRINECMS_BRANCH}..."
git clone --depth 1 ${PEREGRINECMS_REPO}
cd peregrine-cms
git remote set-branches origin ${PEREGRINECMS_BRANCH}
git fetch --depth 1 origin ${PEREGRINECMS_BRANCH}
git checkout ${PEREGRINECMS_BRANCH}

cd ..

echo "Checking out ${THEMECLEANFLEX_REPO} and using ${THEMECLEANFLEX_BRANCH}..."
git clone ${THEMECLEANFLEX_REPO}
cd themeclean-flex
git checkout ${THEMECLEANFLEX_BRANCH}

cd ${SAVE_PWD}
