#!/bin/bash
set -e

docker build -t tensorflow_poc/hadoop_centos6:2.5.2 .

# docker create -t --name=hadoop_centos6 -p 8088:8088 tensorflow_poc/hadoop_centos6:2.5.2
docker create -t --name=hadoop_centos6 tensorflow_poc/hadoop_centos6:2.5.2

docker start hadoop_centos6

docker exec hadoop_centos6 /bin/bash --login hadoop-setup.sh

docker exec hadoop_centos6 /bin/bash --login hadoop-start.sh
