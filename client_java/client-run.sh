#!/bin/bash
set -e

cd /tensorflow-client
./gradlew clean installDist

build/install/tensorflow-client/bin/tensorflow-client $1
