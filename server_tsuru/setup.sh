#!/bin/bash
set -e

rm -rf skeleton/bin/skeleton-server
rm -rf skeleton/deploy
mkdir -p skeleton/deploy

docker cp -L skeleton_project:/skeleton/bazel-bin/server/skeleton-server skeleton/bin/
docker cp skeleton_project:/tmp/monitored/00000002 skeleton/deploy/
