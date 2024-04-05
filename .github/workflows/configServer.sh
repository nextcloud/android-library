#!bin/sh

# Nextcloud Android Library
#
# SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
# SPDX-License-Identifier: MIT
#

php /var/www/html/occ list
OC_PASS=user1 php /var/www/html/occ user:add --password-from-env --display-name='User One' user1
