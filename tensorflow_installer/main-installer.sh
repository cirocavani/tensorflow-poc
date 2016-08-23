#!/bin/bash
set -e

docker start tensorflow_installer_centos6

docker cp tensorflow-bin.sh tensorflow_installer_centos6:/home/tensorflow
docker cp tensorflow-run.sh tensorflow_installer_centos6:/home/tensorflow
docker cp tensorflow-setup.sh tensorflow_installer_centos6:/home/tensorflow
docker cp -L tensorflow-0.9.0-py2-none-linux_x86_64.whl tensorflow_installer_centos6:/home/tensorflow

docker exec -u tensorflow tensorflow_installer_centos6 /home/tensorflow/tensorflow-setup.sh

docker cp tensorflow_installer_centos6:/home/tensorflow/tensorflow.sh .

docker stop tensorflow_installer_centos6
