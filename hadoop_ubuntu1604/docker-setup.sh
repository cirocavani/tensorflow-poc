#!/bin/bash
set -e

echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections
echo 'APT::Install-Recommends "0";' > 01norecommend
mv 01norecommend /etc/apt/apt.conf.d

apt-get update
apt-get upgrade -y

apt-get install -y \
    curl \
    ssh \
    bzip2

# Java install

curl -k -L -H "Cookie: oraclelicense=accept-securebackup-cookie" -O http://download.oracle.com/otn-pub/java/jdk/8u102-b14/jdk-8u102-linux-x64.tar.gz

tar zxf jdk-8u102-linux-x64.tar.gz -C /opt --no-same-owner
rm -f jdk-8u102-linux-x64.tar.gz

echo 'export JAVA_HOME=/opt/jdk1.8.0_102' > /etc/profile.d/java.sh
echo 'export PATH=$PATH:$JAVA_HOME/bin' >>  /etc/profile.d/java.sh
chmod a+x /etc/profile.d/java.sh

echo 'JAVA_HOME=/opt/jdk1.8.0_102' >> /etc/environment

source /etc/profile.d/java.sh

# Hadoop install

curl -O http://archive.apache.org/dist/hadoop/common/hadoop-2.5.2/hadoop-2.5.2.tar.gz

tar zxf hadoop-2.5.2.tar.gz -C /opt --no-same-owner
rm -f hadoop-2.5.2.tar.gz

echo 'export PATH=$PATH:/opt/hadoop-2.5.2/bin:/opt/hadoop-2.5.2/sbin' > /etc/profile.d/hadoop.sh
chmod a+x /etc/profile.d/hadoop.sh

source /etc/profile.d/hadoop.sh
