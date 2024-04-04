#
# Nextcloud Android Library
#
# SPDX-FileCopyrightText: 2026 Tobias Kaminsky <tobias@kaminsky.me>
# SPDX-License-Identifier: MIT
#

scripts/wait_for_emulator.sh
adb logcat -c
adb logcat > logcat.txt &
./gradlew assembleDebug
./gradlew jacocoTestDebugUnitTestReport
./gradlew installDebugAndroidTest
./gradlew createDebugCoverageReport
stat=$?

./gradlew combinedTestReport

exit $stat
