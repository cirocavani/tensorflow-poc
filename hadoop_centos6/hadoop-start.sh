#!/bin/bash
set -e

service sshd start

su - hadoop -c 'start-dfs.sh'
su - hadoop -c 'start-yarn.sh'
