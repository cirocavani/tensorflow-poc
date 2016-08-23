#!/bin/bash
set -e

docker build -t tensorflow_poc/tensorflow:0.9.0 .

docker create -t --name=tflearn_wide_n_deep tensorflow_poc/tensorflow:0.9.0

docker start tflearn_wide_n_deep

docker cp tflearn-setup.sh tflearn_wide_n_deep:/
docker cp tflearn-run.sh tflearn_wide_n_deep:/

docker exec tflearn_wide_n_deep /bin/bash --login tflearn-setup.sh

docker exec tflearn_wide_n_deep /bin/bash --login tflearn-run.sh

docker stop tflearn_wide_n_deep
