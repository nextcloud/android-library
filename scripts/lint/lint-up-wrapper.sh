#!/bin/sh

#1: GIT_USERNAME
#2: GIT_TOKEN
#3: BRANCH
#4: LOG_USERNAME
#5: LOG_PASSWORD
#6: DRONE_BUILD_NUMBER

ruby scripts/lint/lint-up.rb $1 $2 $3

returnValue=$?
if [ $3 = "master" ]; then
    echo "New master at: https://nextcloud.kaminsky.me/index.php/s/TzuLsFAYkOdGNwV"
    curl -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/droneLibLogs/master.html --upload-file build/reports/lint/lint.html
    exit 0
elif [ $returnValue -eq 1 ]; then
    if [ -e $6 ] ; then
        6="master-"$(date +%F)
    fi
    echo "New results at https://nextcloud.kaminsky.me/index.php/s/TzuLsFAYkOdGNwV ->" $6.html
    curl -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/droneLibLogs/$6.html --upload-file build/reports/lint/lint.html
    exit 1
fi
