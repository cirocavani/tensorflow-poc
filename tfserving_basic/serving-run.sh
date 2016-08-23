#!/bin/bash
set -e

cd tensorflow-serving

pip install grpcio==0.15.0

pip install mock

rm -rf /tmp/mnist_model

bazel-bin/tensorflow_serving/example/mnist_export \
    /tmp/mnist_model

bazel-bin/tensorflow_serving/example/mnist_inference \
    --port=9000 \
    /tmp/mnist_model/00000001 \
    > server.log 2>&1 &

bazel-bin/tensorflow_serving/example/mnist_client \
    --num_tests=1000 \
    --server=localhost:9000
