#!/usr/bin/env bash

# SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
# SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
# SPDX-License-Identifier: GPL-3.0-or-later

USER=$1
PASS=$2
ID=$3
BRANCH=$4
TYPE=$5
PR_ID=$6
GIT_USERNAME=$7
GIT_TOKEN=$8
REMOTE_FOLDER=$ID-$TYPE-$BRANCH-$(date +%H-%M)
BRANCH_TYPE=$BRANCH-$TYPE
URL=https://nextcloud.kaminsky.me/remote.php/dav/files/$USER/library-integrationTests

# upload logcat
log_filename=$ID"_logcat.txt.xz"
log_file="${log_filename}"
upload_path="https://nextcloud.kaminsky.me/remote.php/webdav/library-logcat/$log_filename"
xz logcat.txt
mv logcat.txt.xz "$log_file"
curl -u "$USER:$PASS" -X PUT "$upload_path" --upload-file "$log_file"
echo >&2 "Uploaded logcat to https://www.kaminsky.me/nc-dev/library-logcat/$log_filename"

if [ $TYPE = "IT" ]; then
    cd library/build/reports/androidTests/connected/debug
else 
    cd library/build/reports/tests/testDebugUnitTest
fi

if [ $? -ne 0 ]; then
  echo "No reports folder available! Perhaps compilation failed."
  exit 1
fi

find . -type d -exec curl -u $USER:$PASS -X MKCOL $URL/$REMOTE_FOLDER/$(echo {} | sed s#\./##) \;
find . -type f -exec curl -u $USER:$PASS -X PUT $URL/$REMOTE_FOLDER/$(echo {} | sed s#\./##) --upload-file {} \;

echo "Uploaded failing library tests to https://www.kaminsky.me/nc-dev/library-integrationTests/$REMOTE_FOLDER"

curl -u $GIT_USERNAME:$GIT_TOKEN -X POST https://api.github.com/repos/nextcloud/android-library/issues/$PR_ID/comments -d "{ \"body\" : \"$BRANCH_TYPE test failed: https://www.kaminsky.me/nc-dev/library-integrationTests/$REMOTE_FOLDER/debug/ \" }"
exit 1
