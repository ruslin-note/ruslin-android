#!/bin/bash

adb uninstall org.dianqk.mdrenderbenchmark.test

set -e

./gradlew clean
./gradlew :mdrenderbenchmark:assembleReleaseAndroidTest

adb install mdrenderbenchmark/build/outputs/apk/androidTest/release/mdrenderbenchmark-release-androidTest.apk
adb shell am instrument -w org.dianqk.mdrenderbenchmark.test/androidx.benchmark.junit4.AndroidBenchmarkRunner

adb pull /storage/emulated/0/Android/media/org.dianqk.mdrenderbenchmark.test/org.dianqk.mdrenderbenchmark.test-benchmarkData.json
