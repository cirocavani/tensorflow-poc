#!/bin/bash
set -e

adduser -m -d /home/tensorflow tensorflow
passwd -d tensorflow

yum update -y

yum install -y \
    tar \
    gzip \
    bzip2 \
    zip \
    unzip \
    git \
    swig \
    zlib-devel

# GCC 5

yum install -y centos-release-scl

yum install -y devtoolset-4-gcc-c++

echo 'export CC=/opt/rh/devtoolset-4/root/usr/bin/gcc' > /etc/profile.d/gcc.sh
echo 'source /opt/rh/devtoolset-4/enable' >> /etc/profile.d/gcc.sh
chmod a+x /etc/profile.d/gcc.sh

source /etc/profile.d/gcc.sh

echo 'CC=/opt/rh/devtoolset-4/root/usr/bin/gcc' >> /etc/environment

# Java install

curl -k -L -H "Cookie: oraclelicense=accept-securebackup-cookie" -O http://download.oracle.com/otn-pub/java/jdk/8u102-b14/jdk-8u102-linux-x64.rpm

rpm -i jdk-8u102-linux-x64.rpm
rm -f jdk-8u102-linux-x64.rpm

echo 'export JAVA_HOME=/usr/java/jdk1.8.0_102' > /etc/profile.d/java.sh
echo 'export PATH=$PATH:$JAVA_HOME/bin' >>  /etc/profile.d/java.sh
chmod a+x /etc/profile.d/java.sh

source /etc/profile.d/java.sh

echo 'JAVA_HOME=/usr/java/jdk1.8.0_102' >> /etc/environment

# Python install

curl -k -L -O https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh

chmod +x Miniconda2-latest-Linux-x86_64.sh

./Miniconda2-latest-Linux-x86_64.sh -b -f -p /opt/conda

/opt/conda/bin/pip install numpy

# Bazel install

curl -k -L -O https://github.com/bazelbuild/bazel/archive/0.3.1.tar.gz

tar zxf 0.3.1.tar.gz
rm -f 0.3.1.tar.gz

cd bazel-0.3.1/

./compile.sh

mkdir -p /opt/bazel-0.3.1/bin
cp output/bazel /opt/bazel-0.3.1/bin
chmod +x /opt/bazel-0.3.1/bin/bazel

cd ..
rm -rf bazel-0.3.1

echo 'export PATH=$PATH:/opt/bazel-0.3.1/bin' > /etc/profile.d/bazel.sh
chmod a+x /etc/profile.d/bazel.sh

source /etc/profile.d/bazel.sh
