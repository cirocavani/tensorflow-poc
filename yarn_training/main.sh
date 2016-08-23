#!/bin/bash
set -e

docker build -t tensorflow_poc/yarn_training .

docker create -t --name=yarn_training tensorflow_poc/yarn_training

./main-training.sh
