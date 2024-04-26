#!/bin/sh

# Nextcloud Android Library
#
# SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
# SPDX-License-Identifier: MIT
#

SERVER_VERSION="stable16"

php /var/www/html/occ log:manage --level warning

OC_PASS=user1 php /var/www/html/occ user:add --password-from-env --display-name='User One' user1
OC_PASS=user2 php /var/www/html/occ user:add --password-from-env --display-name='User Two' user2
OC_PASS=user3 php /var/www/html/occ user:add --password-from-env --display-name='User Three' user3
OC_PASS=test php /var/www/html/occ user:add --password-from-env --display-name='Test@Test' test@test
OC_PASS=test php /var/www/html/occ user:add --password-from-env --display-name='Test Spaces' 'test test'
php /var/www/html/occ user:setting user2 files quota 1G
php /var/www/html/occ group:add users
php /var/www/html/occ group:adduser users user1
php /var/www/html/occ group:adduser users user2
php /var/www/html/occ group:adduser users test

php /var/www/html/occ app:enable activity

php /var/www/html/occ app:enable text

php /var/www/html/occ app:enable end_to_end_encryption

php /var/www/html/occ app:enable password_policy

php /var/www/html/occ app:enable external
php /var/www/html/occ config:app:set external sites --value="{\"1\":{\"id\":1,\"name\":\"Nextcloud\",\"url\":\"https:\/\/www.nextcloud.com\",\"lang\":\"\",\"type\":\"link\",\"device\":\"\",\"icon\":\"external.svg\",\"groups\":[],\"redirect\":false},\"2\":{\"id\":2,\"name\":\"Forum\",\"url\":\"https:\/\/help.nextcloud.com\",\"lang\":\"\",\"type\":\"link\",\"device\":\"\",\"icon\":\"external.svg\",\"groups\":[],\"redirect\":false}}"

php /var/www/html/occ app:enable groupfolders
php /var/www/html/occ groupfolders:create groupfolder
php /var/www/html/occ groupfolders:group 1 users

git clone -b $SERVER_VERSION https://github.com/nextcloud/notifications.git /var/www/html/apps/notifications/
php /var/www/html/occ app:enable notifications

php /var/www/html/occ app:enable testing
