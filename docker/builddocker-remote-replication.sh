#!/bin/bash
# abort if either image build fails
set -e

./builddocker.sh author,notshared author
./builddocker.sh publish,notshared publish
