#!/bin/bash

###
# #%L
# admin base - Core
# %%
# Copyright (C) 2017 headwire inc.
# %%
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# #L%
###

# Find the location of this script to include the setenv.sh script
DIR="$(dirname "${BASH_SOURCE[0]}")"
. $DIR/setenv.sh

echo ""
echo "Remove any source or target resource"
echo "-------------------------------------------------------------------------------"
echo ""
# Hide output with --silent and > /dev/null
curl --user $SLING_USER:$SLING_PASSWORD -F":operation=delete" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test" > /dev/null

echo ""
echo ""
echo "Verify the removal (there should be no entry with name 'move-resource-test'"
echo "-------------------------------------------------------------------------------"
echo ""
curl --user $SLING_USER:$SLING_PASSWORD "http://$SLING_HOST:$SLING_PORT/content.tidy.1.json"

echo ""
echo ""
echo "Create the basic folders"
echo "-------------------------------------------------------------------------------"
echo ""
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=sling:OrderedFolder" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test" > /dev/null
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=sling:OrderedFolder" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test/source" > /dev/null
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=sling:OrderedFolder" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test/target" > /dev/null
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=sling:OrderedFolder" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test/ref" > /dev/null


echo ""
echo ""
echo "Create the Initial Resources"
echo "-------------------------------------------------------------------------------"
echo ""
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=nt:unstructured" -F"jcr:title=STest1" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test/source/testS1" > /dev/null
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=nt:unstructured" -F"jcr:title=TTest1" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test/target/testT1" > /dev/null
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=nt:unstructured" -F"jcr:title=TTest2" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test/target/testT2" > /dev/null
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=nt:unstructured" -F"jcr:title=TTest3" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test/target/testT3" > /dev/null
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=per:Page" -F"jcr:title=RTest1" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test/ref/testR1" > /dev/null
curl --user $SLING_USER:$SLING_PASSWORD -F"jcr:primaryType=per:PageContent" -F"jcr:title=Reference-Test-1" -F"ref=/content/move-resource-test/source/testS1" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test/ref/testR1/jcr:content" > /dev/null

echo ""
echo ""
echo "Decide how to move"
echo "-------------------------------------------------------------------------------"
echo ""
read -p 'Type of move. Can be either c[hild], b[efore] or a[fter] (child is default): ' typeInput

toPath="/content/move-resource-test/target"
moveType="child"
if [ "$typeInput" == "c" ] || [ "$typeInput" == "child" ]; then
	echo "Selected Type 'Child' which adds testS1 to the end of the list"
	toPath="/content/move-resource-test/target"
	moveType="child"
fi
if [ "$typeInput" == "b" ] || [ "$typeInput" == "before" ]; then
	echo "Selected Type 'Before' which adds testS1 before testT2"
	toPath="/content/move-resource-test/target/testT2"
	moveType="before"
fi
if [ "$typeInput" == "a" ] || [ "$typeInput" == "after" ]; then
	echo "Selected Type 'After' which adds testS1 after testT2"
	toPath="/content/move-resource-test/target/testT2"
	moveType="after"
fi

echo ""
echo ""
echo "Move the Source to the Targe Folder"
echo "-------------------------------------------------------------------------------"
echo ""
curl --user $SLING_USER:$SLING_PASSWORD -X "POST" "http://$SLING_HOST:$SLING_PORT/api/admin/move.json/path///content/move-resource-test/source/testS1//to//$toPath//type//$moveType"

echo ""
echo ""
echo "List the Results"
echo "-------------------------------------------------------------------------------"
echo ""
curl --user $SLING_USER:$SLING_PASSWORD "http://$SLING_HOST:$SLING_PORT/content/move-resource-test.tidy.5.json"

echo ""
echo ""
echo "Let us know when the nodes can be removed"
echo "-------------------------------------------------------------------------------"
echo ""

read -p 'Hit any key to move on and remove any created nodes or enter skip to kepp them: ' proceedWith
if [ "$proceedWith" != "skip" ]; then
	curl --user $SLING_USER:$SLING_PASSWORD -F":operation=delete" --silent "http://$SLING_HOST:$SLING_PORT/content/move-resource-test" > /dev/null
  echo "All test nodes were removed"
fi

echo ""
echo ""
echo "Done"
echo "-------------------------------------------------------------------------------"
echo ""
