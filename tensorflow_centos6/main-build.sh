#!/bin/bash
set -e

docker start tensorflow_build_centos6

docker cp tensorflow-build.sh tensorflow_build_centos6:/home/tensorflow
docker cp tensorflow-0.9.patch tensorflow_build_centos6:/home/tensorflow

docker exec -u tensorflow tensorflow_build_centos6 /bin/bash --login /home/tensorflow/tensorflow-build.sh

docker cp tensorflow_build_centos6:/home/tensorflow/tensorflow-0.9.0-py2-none-linux_x86_64.whl .

docker stop tensorflow_build_centos6
