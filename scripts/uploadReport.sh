#!/usr/bin/env bash

URL=https://nextcloud.kaminsky.me/remote.php/webdav/integrationTests-library
ID=$3
USER=$1
PASS=$2
TYPE=$4

if [ $TYPE = "IT" ]; then
    cd test_client/build/reports/androidTests/connected/
else 
    cd test_client/build/reports/tests/testDebugUnitTest
fi

find . -type d -exec curl -u $USER:$PASS -X MKCOL $URL/$ID/$(echo {} | sed s#\./##) \;
find . -type f -exec curl -u $USER:$PASS -X PUT $URL/$ID/$(echo {} | sed s#\./##) --upload-file {} \;

echo "Uploaded failing library tests to https://nextcloud.kaminsky.me/index.php/s/Pfc8NMxTStWtmMC -> $ID" 
exit 1
