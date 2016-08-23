#!/bin/bash
set -e

cd /tensorflow-yarn

rm -rf bazel-*

bazel build //src/main/java/tensorflow/yarn:yarn-client_deploy.jar

bazel build //src/main/java/tensorflow/yarn:application-master

rm -rf lib
mkdir lib

cp bazel-bin/src/main/java/tensorflow/yarn/{yarn-client_deploy.jar,application-master.jar} lib

rm -rf conf_jar
cp -r conf-local conf_jar

sed -i s/HADOOP_ADDRESS/$1/ conf_jar/*

jar -cf lib/conf.jar -C conf_jar .
rm -rf conf_jar

export HADOOP_USER_NAME=hadoop
bin/yarn-client /tensorflow.sh
