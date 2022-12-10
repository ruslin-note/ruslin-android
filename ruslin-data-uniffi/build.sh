#!/bin/bash

set -e

uniffi-bindgen generate src/ruslin.udl --language kotlin --out-dir ../app/src/main/java
cargo ndk --target arm64-v8a --output-dir ../app/src/main/jniLibs build
