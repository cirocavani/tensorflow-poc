#!/bin/bash
set -e

cd `dirname "$0"`

rm -rf tensorflow
mkdir tensorflow

mkdir tensorflow/bin
mkdir tensorflow/src
mkdir tensorflow/data
mkdir tensorflow/libs

cp tensorflow-run.sh tensorflow/bin/run

chmod +x tensorflow/bin/run

curl -k -L \
    https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh \
    -o tensorflow/bin/Miniconda2-latest-Linux-x86_64.sh

chmod +x tensorflow/bin/Miniconda2-latest-Linux-x86_64.sh

curl -k -L \
    https://raw.githubusercontent.com/tensorflow/tensorflow/master/tensorflow/examples/learn/wide_n_deep_tutorial.py \
    -o tensorflow/src/wide_n_deep_tutorial.py

curl -k -L \
    https://archive.ics.uci.edu/ml/machine-learning-databases/adult/adult.data \
    -o tensorflow/data/adult.data

curl -k -L \
    https://archive.ics.uci.edu/ml/machine-learning-databases/adult/adult.test \
    -o tensorflow/data/adult.test

tensorflow/bin/Miniconda2-latest-Linux-x86_64.sh -b -f -p tf

tf/bin/pip download -d tensorflow/libs tensorflow-0.9.0-py2-none-linux_x86_64.whl
tf/bin/pip download -d tensorflow/libs pandas

cp tensorflow-bin.sh tensorflow.sh
tar czf - tensorflow >> tensorflow.sh
chmod +x tensorflow.sh
