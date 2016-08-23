#!/bin/bash
set -e

yum update -y

yum install -y \
    tar \
    openssh-clients \
    openssh-server \
    bzip2 \
    which

# Java install

curl -k -L -H "Cookie: oraclelicense=accept-securebackup-cookie" -O http://download.oracle.com/otn-pub/java/jdk/7u80-b15/jdk-7u80-linux-x64.rpm

rpm -i jdk-7u80-linux-x64.rpm
rm -f jdk-7u80-linux-x64.rpm

echo 'export JAVA_HOME=/usr/java/jdk1.7.0_80' > /etc/profile.d/java.sh
echo 'export PATH=$PATH:$JAVA_HOME/bin' >>  /etc/profile.d/java.sh
chmod a+x /etc/profile.d/java.sh

source /etc/profile.d/java.sh

echo 'JAVA_HOME=/usr/java/jdk1.7.0_80' >> /etc/environment

# Hadoop install

curl -O http://archive.apache.org/dist/hadoop/common/hadoop-2.5.2/hadoop-2.5.2.tar.gz

tar zxf hadoop-2.5.2.tar.gz -C /opt --no-same-owner
rm -f hadoop-2.5.2.tar.gz

echo 'export PATH=$PATH:/opt/hadoop-2.5.2/bin:/opt/hadoop-2.5.2/sbin' > /etc/profile.d/hadoop.sh
chmod a+x /etc/profile.d/hadoop.sh

source /etc/profile.d/hadoop.sh
