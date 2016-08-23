#!/bin/bash
set -e

docker build -t tensorflow_poc/tfserving_basic .

docker create -t --name=tfserving_basic tensorflow_poc/tfserving_basic

docker start tfserving_basic

docker cp serving-build.sh tfserving_basic:/
docker cp serving-run.sh tfserving_basic:/

docker exec tfserving_basic /bin/bash --login serving-build.sh

docker exec tfserving_basic /bin/bash --login serving-run.sh

docker stop tfserving_basic
