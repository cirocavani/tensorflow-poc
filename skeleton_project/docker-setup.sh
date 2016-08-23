#!/bin/bash
set -e

echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections
echo 'APT::Install-Recommends "0";' > 01norecommend
mv 01norecommend /etc/apt/apt.conf.d

apt-get update
apt-get upgrade -y

apt-get install -y \
    build-essential \
    file \
    curl \
    git \
    ca-certificates \
    libfreetype6-dev \
    libpng12-dev \
    libzmq3-dev \
    pkg-config \
    swig \
    unzip \
    zip \
    zlib1g-dev

# Java install

curl -k -L -H "Cookie: oraclelicense=accept-securebackup-cookie" -O http://download.oracle.com/otn-pub/java/jdk/8u102-b14/jdk-8u102-linux-x64.tar.gz

tar zxf jdk-8u102-linux-x64.tar.gz -C /opt --no-same-owner
rm -f jdk-8u102-linux-x64.tar.gz

echo 'export JAVA_HOME=/opt/jdk1.8.0_102' > /etc/profile.d/java.sh
echo 'export PATH=$PATH:$JAVA_HOME/bin' >>  /etc/profile.d/java.sh
chmod a+x /etc/profile.d/java.sh

source /etc/profile.d/java.sh

# Python install

curl -k -L -O https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh

chmod +x Miniconda2-latest-Linux-x86_64.sh

./Miniconda2-latest-Linux-x86_64.sh -b -f -p /opt/conda

echo 'export PATH=$PATH:/opt/conda/bin' >>  /etc/profile.d/conda.sh
chmod a+x /etc/profile.d/conda.sh

source /etc/profile.d/conda.sh

pip install numpy
pip install grpcio==0.15.0
pip install mock

# Bazel install

curl -k -L -O https://github.com/bazelbuild/bazel/releases/download/0.3.1/bazel-0.3.1-installer-linux-x86_64.sh

chmod +x bazel-0.3.1-installer-linux-x86_64.sh

./bazel-0.3.1-installer-linux-x86_64.sh --prefix=/opt/bazel-0.3.1
rm -f bazel-0.3.1-installer-linux-x86_64.sh

echo 'export PATH=$PATH:/opt/bazel-0.3.1/bin' > /etc/profile.d/bazel.sh
chmod a+x /etc/profile.d/bazel.sh

source /etc/profile.d/bazel.sh
