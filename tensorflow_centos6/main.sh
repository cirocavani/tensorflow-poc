#!/bin/bash
set -e

docker build -t tensorflow_poc/build_centos6 .

docker create -t --name=tensorflow_build_centos6 tensorflow_poc/build_centos6

./main-build.sh
