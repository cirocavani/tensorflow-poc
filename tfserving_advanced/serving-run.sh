#!/bin/bash
set -e

cd tensorflow-serving

pip install grpcio==0.15.0

pip install mock

rm -rf /tmp/mnist_model

bazel-bin/tensorflow_serving/example/mnist_export \
    --training_iteration=100 \
    --export_version=1 \
    /tmp/mnist_model

bazel-bin/tensorflow_serving/example/mnist_export \
    --training_iteration=2000 \
    --export_version=2 \
    /tmp/mnist_model

rm -rf /tmp/monitored
mkdir /tmp/monitored

cp -r /tmp/mnist_model/00000001 /tmp/monitored

bazel-bin/tensorflow_serving/example/mnist_inference_2 \
    --port=9000 \
    /tmp/monitored \
     > server.log 2>&1 &

sleep 2

bazel-bin/tensorflow_serving/example/mnist_client \
    --num_tests=1000 \
    --server=localhost:9000 \
    --concurrency=10

cp -r /tmp/mnist_model/00000002 /tmp/monitored

sleep 2

bazel-bin/tensorflow_serving/example/mnist_client \
    --num_tests=1000 \
    --server=localhost:9000 \
    --concurrency=10
