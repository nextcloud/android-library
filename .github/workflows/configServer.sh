#!/bin/sh

# Nextcloud Android Library
#
# SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
# SPDX-License-Identifier: MIT
#

wget -O /etc/apt/trusted.gpg.d/php.gpg https://packages.sury.org/php/apt.gpg
apt-get update && apt-get install -y composer
mkdir /var/www/.nvm /var/www/.npm
touch /var/www/.bashrc
chown -R 33:33 /var/www/.nvm /var/www/.npm /var/www/.bashrc

rm data -rf
rm config/config.php
BRANCH="$1" /usr/local/bin/initnc.sh
