#!/usr/bin/env bash

BRANCH=$1
LOG_USERNAME=$2
LOG_PASSWORD=$3
BUILD_NUMBER=$4
PR_NUMBER=$5


stableBranch="master"
repository="library"

curl "https://www.kaminsky.me/nc-dev/$repository-findbugs/$stableBranch.xml" -o "/tmp/$stableBranch.xml"
ruby scripts/analysis/spotbugs-up.rb "$stableBranch"
spotbugsValue=$?

# exit codes:
# 0: count was reduced
# 1: count was increased
# 2: count stayed the same

source scripts/lib.sh

echo "Branch: $BRANCH"

if [ "$BRANCH" = $stableBranch ]; then
    echo "New spotbugs result for $stableBranch at: https://www.kaminsky.me/nc-dev/$repository-findbugs/$stableBranch.html"
    curl -u "${LOG_USERNAME}:${LOG_PASSWORD}" -X PUT https://nextcloud.kaminsky.me/remote.php/webdav/$repository-findbugs/$stableBranch.html --upload-file library/build/reports/spotbugs/spotbugs.html
    curl 2>/dev/null -u "${LOG_USERNAME}:${LOG_PASSWORD}" -X PUT "https://nextcloud.kaminsky.me/remote.php/webdav/$repository-findbugs/$stableBranch.xml" --upload-file library/build/reports/spotbugs/spotbugs.html
else
    if [ -e "${BUILD_NUMBER}" ]; then
        6=$stableBranch"-"$(date +%F)
    fi

    echo "New spotbugs results at https://www.kaminsky.me/nc-dev/$repository-findbugs/${BUILD_NUMBER}.html"
    curl 2>/dev/null -u "${LOG_USERNAME}:${LOG_PASSWORD}" -X PUT "https://nextcloud.kaminsky.me/remote.php/webdav/$repository-findbugs/${BUILD_NUMBER}.html" --upload-file library/build/reports/spotbugs/spotbugs.html

    # delete all old comments, starting with Codacy
    oldComments=$(curl_gh -X GET "https://api.github.com/repos/nextcloud/$repository/issues/${PR_NUMBER}/comments" | jq '.[] | select((.user.login | contains("github-actions")) and  (.body | test("<h1>Codacy.*"))) | .id')

    echo "$oldComments" | while read -r comment ; do
        curl_gh -X DELETE "https://api.github.com/repos/nextcloud/$repository/issues/comments/$comment"
    done

    # spotbugs file must exist
    if [ ! -s library/build/reports/spotbugs/spotbugs.html ] ; then
        echo "spotbugs.html file is missing!"
        exit 1
    fi

    # add comment with results
    spotbugsResult="<h1>SpotBugs</h1>$(scripts/analysis/spotbugsComparison.py "/tmp/$stableBranch.xml" library/build/reports/spotbugs/debug.xml --link-new "https://www.kaminsky.me/nc-dev/$repository-findbugs/${BUILD_NUMBER}.html" --link-base "https://www.kaminsky.me/nc-dev/$repository-findbugs/$stableBranch.html")"

    if ( [ $spotbugsValue -eq 1 ] ) ; then
        spotbugsMessage="<h1>SpotBugs increased!</h1>"
    fi
    
    # check for NotNull
    if [[ $(grep org.jetbrains.annotations library/src/main/* -irl | wc -l) -gt 0 ]] ; then
        notNull="org.jetbrains.annotations.* is used. Please use androidx.annotation.* instead.<br><br>"
    fi

    bodyContent="$spotbugsResult $spotbugsMessage $gplayLimitation $notNull"
    echo "$bodyContent" >> "$GITHUB_STEP_SUMMARY"
    payload="{ \"body\" : \"$bodyContent\" }"
    curl_gh -X POST "https://api.github.com/repos/nextcloud/$repository/issues/${PR_NUMBER}/comments" -d "$payload"

    if [ ! -z "$gplayLimitation" ]; then
        exit 1
    fi

    if [ -n "$notNull" ]; then
        exit 1
    fi

    if [ $spotbugsValue -eq 2 ]; then
        exit 0
    else
        exit $spotbugsValue
    fi
fi
