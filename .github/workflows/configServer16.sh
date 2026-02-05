#!/bin/sh

# Nextcloud Android Library
#
# SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
# SPDX-License-Identifier: MIT
#

rm data -rf
rm config/config.php
BRANCH="stable16" /initnc.sh
