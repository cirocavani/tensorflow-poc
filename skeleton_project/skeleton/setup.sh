#!/bin/bash
set -e

git clone --recurse-submodules https://github.com/tensorflow/serving.git
ln -s serving/tensorflow .

cd serving
git checkout 7bbed91

cd tensorflow
git checkout c5983f8

export PYTHON_BIN_PATH=/opt/conda/bin/python
export TF_NEED_GCP=0
export TF_NEED_CUDA=0
./configure
