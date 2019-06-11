#!/bin/bash

# Set the Environment Variable needed here
#
# Please adjust these values to your need and rename this file to setenv.sh
#
# The path to the SLing Feature Model and the Content Package Converter are
# checked out, built and then referenced here to allow the user to use any
# version the user desires as the Feature Model is under heavy development
#
# When you check out the Converters you can select the version you want and
# checkout a release from a tag.

# You can set your Sling Dev Home folder here and if all Feature Model Converter and Sling Starter is there
# you are all set
export SLING_DEV_HOME=Fill me in or comment me out

# Home of the Sling Feature Model Converter. YOu must check this out from Github:
# github.com/apache/sling-org-apache-sling-feature-modelconverter
# checkout branch "master" and built it prior to using it here with 'mvn clean install'
export SFMC_HOME=$SLING_DEV_HOME/sling-org-apache-sling-feature-modelconverter

# Home of the Sling Feature Content Package Converter. You must check this out from Github:
# github.com/apache/sling-org-apache-sling-feature-cpconverter
# checkout branch "master" and built it prior to using it here with 'mvn clean install'
export SFCPC_HOME=$SLING_DEV_HOME/sling-org-apache-sling-feature-cpconverter

# Home of the SLing Starter. This is source of the Sling Provisioning Model
# which is then converted to a Feature Model. Check it out from GitHub:
# github.com/apache/sling-org-apache-sling-starter
# There is not need to build it as this is just providing the source Provisition
# Model.
export SLING_STARTER_HOME=$SLING_DEV_HOME/sling-org-apache-sling-starter
