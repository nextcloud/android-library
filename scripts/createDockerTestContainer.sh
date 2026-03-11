#!/bin/bash

# Nextcloud Android Library
#
# SPDX-FileCopyrightText: 2024 Tobias Kaminsky <tobias.kaminsky@nextcloud.com>
# SPDX-License-Identifier: MIT
#

if [[ $# -ne 1 ]]; then
    echo "please specify stable29 or master as first parameter"
    exit
fi

docker stop testNC
docker rm testNC
docker run --name=testNC ghcr.io/nextcloud/continuous-integration-shallow-server:latest &
sleep 60

docker cp ../.github/workflows/configServer.sh testNC:/tmp/
docker exec testNC chmod +x /tmp/configServer.sh
docker exec testNC /tmp/configServer.sh $1
docker cp ../.github/workflows/configNC_$1.sh testNC:/tmp/
docker exec testNC chmod +x /tmp/configNC_$1.sh
docker exec --user www-data testNC /tmp/configNC_$1.sh

echo "Docker IP Address is:"
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' testNC
