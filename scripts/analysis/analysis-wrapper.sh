#!/bin/sh

#1: GIT_USERNAME
#2: GIT_TOKEN
#3: BRANCH
#4: LOG_USERNAME
#5: LOG_PASSWORD
#6: DRONE_BUILD_NUMBER
#7: PULL_REQUEST_NUMBER

stableBranch="stable-2.14"
repository="library"

ruby scripts/analysis/findbugs-up.rb $1 $2 $3
findbugsValue=$?

# exit codes:
# 0: count was reduced
# 1: count was increased
# 2: count stayed the same

echo "Branch: $3"

if [ $3 = $stableBranch ]; then
    echo "New SpotBugs result for $stableBranch at: https://www.kaminsky.me/nc-dev/$repository-findbugs/$stableBranch.html"
    curl -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/dav/files/$4/$repository-findbugs/$stableBranch.html --upload-file library/build/reports/spotbugs/spotbugs.html

    summary=$(sed -n "/<h1>Summary<\/h1>/,/<h1>Warnings<\/h1>/p" library/build/reports/spotbugs/spotbugs.html | head -n-1 | sed s'/<\/a>//'g | sed s'/<a.*>//'g | sed s"/Summary/SpotBugs ($stableBranch)/" | tr "\"" "\'" | tr -d "\r\n" | sed 's/^ *//')
    curl -u $4:$5 -X PUT -d "$summary" https://nextcloud.kaminsky.me/remote.php/dav/files/$4/$repository-findbugs/findbugs-summary-$stableBranch.html
else
    if [ -e $6 ]; then
        6=$stableBranch"-"$(date +%F)
    fi
    echo "New SpotBugs results at https://www.kaminsky.me/nc-dev/$repository-findbugs/$6.html"
    curl 2>/dev/null -u $4:$5 -X PUT https://nextcloud.kaminsky.me/remote.php/dav/files/$4/$repository-findbugs/$6.html --upload-file library/build/reports/spotbugs/spotbugs.html

    # delete all old comments
    oldComments=$(curl 2>/dev/null -u $1:$2 -X GET https://api.github.com/repos/nextcloud/android-library/issues/$7/comments | jq '.[] | (.id |tostring) + "|" + (.user.login | test("nextcloud-android-bot") | tostring) ' | grep true | tr -d "\"" | cut -f1 -d"|")

    echo $oldComments | while read comment ; do
        curl 2>/dev/null -u $1:$2 -X DELETE https://api.github.com/repos/nextcloud/android-library/issues/comments/$comment
    done

    # spotbugs file must exist
    if [ ! -s library/build/reports/spotbugs/spotbugs.html ] ; then
        echo "spotbugs.html file is missing!"
        exit 1
    fi

    # add comment with results
    findbugsResultNew=$(sed -n "/<h1>Summary<\/h1>/,/<h1>Warnings<\/h1>/p" library/build/reports/spotbugs/spotbugs.html |head -n-1 | sed s'/<\/a>//'g | sed s'/<a.*>//'g | sed s"#Summary#<a href=\"https://www.kaminsky.me/nc-dev/$repository-findbugs/$6.html\">SpotBugs</a> (new)#" | tr "\"" "\'" | tr -d "\n" | sed 's/^ *//')
    findbugsResultOld=$(curl 2>/dev/null https://www.kaminsky.me/nc-dev/$repository-findbugs/findbugs-summary-$stableBranch.html | tr "\"" "\'" | tr -d "\r\n" | sed s"#SpotBugs#<a href=\"https://www.kaminsky.me/nc-dev/$repository-findbugs/$stableBranch.html\">SpotBugs</a>#" | tr "\"" "\'" | tr -d "\n" | sed 's/^ *//')

    if ( [ $findbugsValue -eq 1 ] ) ; then
        findbugsMessage="<h1>SpotBugs increased!</h1>"
    fi

    curl -u $1:$2 -X POST https://api.github.com/repos/nextcloud/android-library/issues/$7/comments -d "{ \"body\" : \"$findbugsResultNew $findbugsResultOld $findbugsMessage \" }"

    if [ $findbugsValue -eq 2 ]; then
        exit 0
    else
        exit $findbugsValue
    fi
fi
