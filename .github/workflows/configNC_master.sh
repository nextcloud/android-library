#!/bin/sh

# Nextcloud Android Library
#
# SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
# SPDX-License-Identifier: MIT
#



curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.37.2/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

source ~/.bashrc; nvm install node

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

git clone --depth=1 -b master https://github.com/nextcloud/activity.git /var/www/html/apps/activity/
php /var/www/html/occ app:enable activity

git clone --depth=1 -b main https://github.com/nextcloud/text.git /var/www/html/apps/text/
php /var/www/html/occ app:enable text

git clone --depth=1 -b master https://github.com/nextcloud/end_to_end_encryption/  /var/www/html/apps/end_to_end_encryption/
php /var/www/html/occ app:enable end_to_end_encryption

git clone --depth=1 -b master https://github.com/nextcloud/password_policy/  /var/www/html/apps/password_policy/
php /var/www/html/occ app:enable password_policy

git clone --depth=1 -b master https://github.com/nextcloud/external/  /var/www/html/apps/external/
cd /var/www/html/apps/external; composer install --no-dev
php /var/www/html/occ app:enable external
php /var/www/html/occ config:app:set external sites --value="{\"1\":{\"id\":1,\"name\":\"Nextcloud\",\"url\":\"https:\/\/www.nextcloud.com\",\"lang\":\"\",\"type\":\"link\",\"device\":\"\",\"icon\":\"external.svg\",\"groups\":[],\"redirect\":false},\"2\":{\"id\":2,\"name\":\"Forum\",\"url\":\"https:\/\/help.nextcloud.com\",\"lang\":\"\",\"type\":\"link\",\"device\":\"\",\"icon\":\"external.svg\",\"groups\":[],\"redirect\":false}}"

git clone --depth=1 -b main https://github.com/nextcloud/files_lock.git /var/www/html/apps/files_lock/
php /var/www/html/occ app:enable -f files_lock

git clone --depth=1 https://github.com/nextcloud/groupfolders.git /var/www/html/apps/groupfolders/
php /var/www/html/occ app:enable -f groupfolders
php /var/www/html/occ groupfolders:create groupfolder
php /var/www/html/occ groupfolders:group 1 users

git clone --depth=1  https://github.com/nextcloud/notifications.git /var/www/html/apps/notifications/
php /var/www/html/occ app:enable -f notifications
php /var/www/html/occ notification:generate test -d test

git clone --depth=1 https://github.com/nextcloud/photos.git /var/www/html/apps/photos/
cd /var/www/html/apps/photos; composer install --no-dev
php /var/www/html/occ app:enable -f photos

git clone --depth=1 https://github.com/nextcloud/assistant.git /var/www/html/apps/assistant/
cd /var/www/html/apps/assistant; source ~/.bashrc; make
php /var/www/html/occ app:enable -f assistant

php /var/www/html/occ app:enable -f testing

git clone --depth 1 https://github.com/nextcloud/files_downloadlimit.git /var/www/html/apps/files_downloadlimit/
php /var/www/html/occ app:enable -f files_downloadlimit

git clone --depth 1 -b master https://github.com/nextcloud/recommendations.git /var/www/html/apps/recommendations/
php /var/www/html/occ app:enable -f recommendations

php /var/www/html/occ config:system:set ratelimit.protection.enabled --value false --type bool
