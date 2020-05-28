#!/usr/bin/env bash

URL=https://nextcloud.kaminsky.me/remote.php/webdav/library-integrationTests
ID=$3
USER=$1
PASS=$2
TYPE=$4
REMOTE_FOLDER=$ID-$TYPE

if [ $TYPE = "IT" ]; then
    cd build/reports/androidTests/connected/
else 
    cd build/reports/tests/testDebugUnitTest
fi

find . -type d -exec curl -u $USER:$PASS -X MKCOL $URL/$REMOTE_FOLDER/$(echo {} | sed s#\./##) \;
find . -type f -exec curl -u $USER:$PASS -X PUT $URL/$REMOTE_FOLDER/$(echo {} | sed s#\./##) --upload-file {} \;

echo "Uploaded failing library tests to https://www.kaminsky.me/nc-dev/library-integrationTests/$REMOTE_FOLDER"

curl -u $6:$7 -X POST https://api.github.com/repos/nextcloud/android-library/issues/$5/comments -d "{ \"body\" : \"$TYPE test failed: https://www.kaminsky.me/nc-dev/library-integrationTests/$REMOTE_FOLDER \" }"
exit 1
