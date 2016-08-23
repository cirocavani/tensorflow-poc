#!/bin/bash
set -e

adduser -m -d /home/tensorflow tensorflow
passwd -d tensorflow

yum update -y

yum install -y \
    tar \
    gzip \
    bzip2 \
    curl
