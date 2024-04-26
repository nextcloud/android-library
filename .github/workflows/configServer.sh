#!/bin/sh

# Nextcloud Android Library
#
# SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
# SPDX-License-Identifier: MIT
#

apt-get update && apt-get install -y composer
mkdir /var/www/.nvm /var/www/.npm
touch /var/www/.bashrc
chown -R 33:33 /var/www/.nvm /var/www/.npm /var/www/.bashrc

rm data -rf
rm config/config.php
BRANCH="stable27" /usr/local/bin/initnc.sh
