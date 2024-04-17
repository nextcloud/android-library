#!/bin/bash

# SPDX-FileCopyrightText: 2017-2024 Nextcloud GmbH and Nextcloud contributors
# SPDX-FileCopyrightText: 2017 Tobias Kaminsky <tobias@kaminsky.me>
# SPDX-License-Identifier: GPL-3.0-or-later

# $1: username, $2: password/token, $3: pull request number

if [ -z $3 ] ; then
    echo "stable-2.19";
else
    curl 2>/dev/null -u $1:$2 https://api.github.com/repos/nextcloud/android-library/pulls/$3 | grep \"ref\": | grep -v '"stable-2.19"' | cut -d"\"" -f4
fi
