#!/bin/bash
set -e

cd skeleton

rm -rf /tmp/mnist_model

python bazel-bin/train/mnist_export \
    --training_iteration=100 \
    --export_version=1 \
    /tmp/mnist_model

python bazel-bin/train/mnist_export \
    --training_iteration=2000 \
    --export_version=2 \
    /tmp/mnist_model

rm -rf /tmp/monitored
mkdir /tmp/monitored

cp -r /tmp/mnist_model/00000001 /tmp/monitored

bazel-bin/server/skeleton-server \
    --port=9000 \
    /tmp/monitored \
     > server.log 2>&1 &

sleep 2

python bazel-bin/client/skeleton_client \
    --num_tests=1000 \
    --server=localhost:9000 \
    --concurrency=10

cp -r /tmp/mnist_model/00000002 /tmp/monitored

sleep 2

python bazel-bin/client/skeleton_client \
    --num_tests=1000 \
    --server=localhost:9000 \
    --concurrency=10
