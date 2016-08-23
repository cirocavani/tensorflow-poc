#!/bin/bash
set -e

docker start yarn_training

docker cp tensorflow-yarn/ yarn_training:/
docker cp tensorflow-run.sh yarn_training:/
docker cp -L tensorflow.sh yarn_training:/

# HADOOP_ADDRESS="$(docker exec hadoop_ubuntu1604 hostname -i)"
HADOOP_ADDRESS="$(docker exec hadoop_centos6 hostname -i)"

docker exec yarn_training /bin/bash --login /tensorflow-run.sh $HADOOP_ADDRESS

docker stop yarn_training
