#!/bin/bash
set -e

TF_HOME="$(cd `dirname "$0"`/..; pwd)"

$TF_HOME/bin/Miniconda2-latest-Linux-x86_64.sh -b -f -p tf

tf/bin/python2.7 tf/bin/pip install --no-cache-dir --no-index --find-links $TF_HOME/libs tensorflow

tf/bin/python2.7 tf/bin/pip install --no-cache-dir --no-index --find-links $TF_HOME/libs pandas

tf/bin/python2.7 $TF_HOME/src/wide_n_deep_tutorial.py \
    --model_type=wide_n_deep \
    --train_data=$TF_HOME/data/adult.data \
    --test_data=$TF_HOME/data/adult.test \
    --model_dir=model/
