#!/bin/bash
set -e

docker build -t tensorflow_poc/installer_centos6 .

docker create -t --name=tensorflow_installer_centos6 tensorflow_poc/installer_centos6

./main-installer.sh
