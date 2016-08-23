#!/bin/bash
set -e

# tsuru app-create tf-skeleton python -p huge -t bigdata -o bigdata

tsuru app-deploy -a tf-skeleton Procfile skeleton
