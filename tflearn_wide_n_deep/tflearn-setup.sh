#!/bin/bash
set -e

mkdir -p tflearn/src
mkdir -p tflearn/data

curl -k -L \
    https://raw.githubusercontent.com/tensorflow/tensorflow/master/tensorflow/examples/learn/wide_n_deep_tutorial.py \
    -o tflearn/src/wide_n_deep_tutorial.py

curl -k -L \
    https://archive.ics.uci.edu/ml/machine-learning-databases/adult/adult.data \
    -o tflearn/data/adult.data

curl -k -L \
    https://archive.ics.uci.edu/ml/machine-learning-databases/adult/adult.test \
    -o tflearn/data/adult.test

echo "Done!"
