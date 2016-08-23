#!/bin/bash
set -e

# Endereço da API do Tsuru
export TSURU_HOST=
# Token do Usuário
export TSURU_USER_TOKEN=

cd $(dirname "$0")/tensorflow-tsuru

./gradlew run
