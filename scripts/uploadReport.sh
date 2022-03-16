#!/usr/bin/env bash

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

if [ $TYPE = "IT" ]; then
    cd library/build/reports/androidTests/connected
else 
    cd library/build/reports/tests/testDebugUnitTest
fi

find . -type d -exec curl -u $USER:$PASS -X MKCOL $URL/$REMOTE_FOLDER/$(echo {} | sed s#\./##) \;
find . -type f -exec curl -u $USER:$PASS -X PUT $URL/$REMOTE_FOLDER/$(echo {} | sed s#\./##) --upload-file {} \;

echo "Uploaded failing library tests to https://www.kaminsky.me/nc-dev/library-integrationTests/$REMOTE_FOLDER"

curl -u $GIT_USERNAME:$GIT_TOKEN -X POST https://api.github.com/repos/nextcloud/android-library/issues/$PR_ID/comments -d "{ \"body\" : \"$BRANCH_TYPE test failed: https://www.kaminsky.me/nc-dev/library-integrationTests/$REMOTE_FOLDER \" }"
exit 1
