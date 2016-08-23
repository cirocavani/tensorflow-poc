#!/bin/bash
set -e

/etc/init.d/ssh start

su - hadoop -c 'start-dfs.sh'
su - hadoop -c 'start-yarn.sh'
