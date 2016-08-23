#!/bin/bash
set -e

cd skeleton

./setup.sh

bazel build server:skeleton-server

bazel build train:mnist_export

bazel build client:skeleton_client
