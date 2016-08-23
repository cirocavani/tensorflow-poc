#!/bin/bash
set -e

docker build -t tensorflow_poc/hadoop_ubuntu1604:2.5.2 .

#docker create -t --name=hadoop_ubuntu1604 -p 8088:8088 tensorflow_poc/hadoop_ubuntu1604:2.5.2
docker create -t --name=hadoop_ubuntu1604 tensorflow_poc/hadoop_ubuntu1604:2.5.2

docker start hadoop_ubuntu1604

docker exec hadoop_ubuntu1604 /bin/bash --login hadoop-setup.sh

docker exec hadoop_ubuntu1604 /bin/bash --login hadoop-start.sh
