#!/bin/bash
set -e

rm -rf /home/tensorflow/tensorflow-0.9

git clone --recursive --branch r0.9 https://github.com/tensorflow/tensorflow /home/tensorflow/tensorflow-0.9

cd /home/tensorflow/tensorflow-0.9
git apply ../tensorflow-0.9.patch

export PYTHON_BIN_PATH=/opt/conda/bin/python
export TF_NEED_GCP=0
export TF_NEED_CUDA=0

./configure

bazel build -c opt //tensorflow/tools/pip_package:build_pip_package

bazel-bin/tensorflow/tools/pip_package/build_pip_package /home/tensorflow

mv /home/tensorflow/tensorflow-0.9.0-py2-none-{any,linux_x86_64}.whl
