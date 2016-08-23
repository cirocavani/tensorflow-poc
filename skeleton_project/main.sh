#!/bin/bash
set -e

docker build -t tensorflow_poc/skeleton_project .

docker create -t --name=skeleton_project tensorflow_poc/skeleton_project

docker start skeleton_project

docker cp skeleton/ skeleton_project:/

docker cp skeleton-build.sh skeleton_project:/

docker cp skeleton-run.sh skeleton_project:/

docker exec skeleton_project /bin/bash --login skeleton-build.sh

docker exec skeleton_project /bin/bash --login skeleton-run.sh

docker stop skeleton_project
