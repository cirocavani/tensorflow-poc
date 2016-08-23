#!/bin/bash
set -e

git clone --recurse-submodules https://github.com/tensorflow/serving tensorflow-serving

cd tensorflow-serving
git checkout 7bbed91

cd tensorflow
git checkout c5983f8

export PYTHON_BIN_PATH=/usr/bin/python
export TF_NEED_GCP=0
export TF_NEED_CUDA=0
./configure
cd ..

bazel build //tensorflow_serving/example:mnist_export

bazel build //tensorflow_serving/example:mnist_inference

bazel build //tensorflow_serving/example:mnist_client
