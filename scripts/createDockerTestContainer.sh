#!/bin/bash

# Nextcloud Android Library
#
# SPDX-FileCopyrightText: 2026 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
# SPDX-License-Identifier: MIT
#

if [[ $# -ne 1 ]]; then
    echo "please specify as first parameter: stable22, stable33, master"
    exit
fi

docker stop testNC
docker rm testNC

if [ $1 = 'stable22' ]; then 
    docker run --name=testNC ghcr.io/nextcloud/continuous-integration-shallow-server-php8.0:1 &
else 
    docker run --name=testNC ghcr.io/nextcloud/continuous-integration-shallow-server-php8.2:1 &
fi

sleep 60

docker cp ../.github/workflows/configServer.sh testNC:/tmp/
docker exec testNC chmod +x /tmp/configServer.sh
docker exec testNC /tmp/configServer.sh $1
docker cp ../.github/workflows/configNC.sh testNC:/tmp/
docker exec testNC chmod +x /tmp/configNC.sh
docker exec --user www-data testNC /tmp/configNC.sh $1
docker exec testNC -dt /usr/local/bin/run.sh

echo "Docker IP Address is:"
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' testNC
