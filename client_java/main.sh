#!/bin/bash
set -e

docker build -t tensorflow_poc/client_java .

docker create -t --name=client_java tensorflow_poc/client_java

docker start client_java

docker cp tensorflow-client/ client_java:/
docker cp client-run.sh client_java:/

# Server start
docker start skeleton_project

docker exec skeleton_project /bin/bash --login -c "
cd /skeleton
bazel-bin/server/skeleton-server \
    --port=9000 \
    /tmp/monitored \
     > server.log 2>&1 &
"

# Client run
SERVER_ADDRESS="$(docker exec skeleton_project hostname -i)"
docker exec client_java /bin/bash --login /client-run.sh $SERVER_ADDRESS

docker stop client_java
docker stop skeleton_project
