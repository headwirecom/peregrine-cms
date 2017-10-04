# Peregrine Integration Test Suite

## Introduction

The ITs for Peregrine are designed to make sure that Peregrine components work
and keep on working. The will be executed aside from the regular build as
they require code to be deployed into Sling.

## Requirements

All ITs expect that the code is locally built and installed against
the 
## Client-Side Tests

These ITs are executing against the Rest API of Peregrine
and test the resulting data structure in- or outside of Sling.

### Execution with Standalone Sling

Before the ITs are executed this profile will create a local
Sling instance and deploy all the packages and bundles.
It will use a temporary port that is printed out at the
beginning of the test suite.

This requires no special attention and should run through
without any problems. That said it takes quite a while
to run through because it starts Sling from scratch and then
deploys are the bundles and packages.

The ITs are execute this way:

    mvn clean install -P standalone

### Execution with a Local Sling instance

In order to develop and fix tests / code the ITs can be
executed against an already running and fully stocked Sling
instance:

    mvn clean install -P local

This requires the following:

1. Sling installed and running
1. Run Mode 'it' is added to Sling (sling.properties -> sling.run.modes=it,...)
1. All Peregrine bundles and packages installed and running
1. Testing Config Client built

**Attention**: keep in mind that this will add additional nodes
and configurations around. Also the ITs will delete any test folders
before they are executed but not when they are done so that these nodes
can be inspected to see what is going on.

## Server-Side Tests

Some features cannot be tested through the Rest API and for that
the server-side tests are for. They work quite nicely but the feedback
to the client is not very good.

For now the serer-side tests can only be executed with temporary (standalone)
Sling instance. There is no profile so it can be executed with:

    mvn clean install


