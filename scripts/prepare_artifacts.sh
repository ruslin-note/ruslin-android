#!/usr/bin/env bash

set -e

pushd app/build/outputs/mapping/release
zip mapping.zip *.txt
popd

mv app/build/outputs/bundle/release/app-release.aab app/build/outputs/bundle/release/x-app-release.aab
mv app/build/outputs/mapping/release/mapping.zip app/build/outputs/mapping/release/x-mapping.zip
# FIXME: Unable to retrieve this in CI.
# mv app/build/outputs/native-debug-symbols/release/native-debug-symbols.zip app/build/outputs/native-debug-symbols/release/x-native-debug-symbols.zip
