#!/bin/bash

set -e

gcloud beta firebase test android run \
--app dummy.apk \
--test mdrenderbenchmark/build/outputs/apk/androidTest/release/mdrenderbenchmark-release-androidTest.apk \
--test-runner-class androidx.benchmark.junit4.AndroidBenchmarkRunner \
--device model=redfin,version=30,locale=en,orientation=portrait \
--directories-to-pull /sdcard/Download \
--environment-variables additionalTestOutputDir=/sdcard/Download,no-isolated-storage=true \
--timeout 20m
