#!/bin/bash

python tflearn/src/wide_n_deep_tutorial.py \
    --model_type=wide_n_deep \
    --train_data=tflearn/data/adult.data \
    --test_data=tflearn/data/adult.test \
    --model_dir=tflearn/model/
