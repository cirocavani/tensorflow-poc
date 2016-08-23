#!/bin/bash
set -e

docker build -t tensorflow_poc/tfserving_advanced .

docker create -t --name=tfserving_advanced tensorflow_poc/tfserving_advanced

docker start tfserving_advanced

docker cp serving-build.sh tfserving_advanced:/
docker cp serving-run.sh tfserving_advanced:/

docker exec tfserving_advanced /bin/bash --login serving-build.sh

docker exec tfserving_advanced /bin/bash --login serving-run.sh

docker stop tfserving_advanced
