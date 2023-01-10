#!/bin/bash

set -e

# https://github.com/godot-rust/gdnative/pull/920/files

export ANDROID_HOME=$HOME/Android/Sdk
export ANDROID_NDK_VERSION=25.1.8937393

find -L $ANDROID_HOME/ndk/$ANDROID_NDK_VERSION -name libunwind.a -execdir sh -c 'echo "INPUT(-lunwind)" > libgcc.a' \;

export ANDROID_NDK_TOOLCHAIN_BIN=$ANDROID_HOME/ndk/$ANDROID_NDK_VERSION/toolchains/llvm/prebuilt/linux-x86_64/bin

export AR=$ANDROID_NDK_TOOLCHAIN_BIN/llvm-ar

# export CC=$ANDROID_NDK_TOOLCHAIN_BIN/armv7a-linux-androideabi28-clang
# export CXX=$ANDROID_NDK_TOOLCHAIN_BIN/armv7a-linux-androideabi28-clang++
# export CARGO_TARGET_ARMV7_LINUX_ANDROIDEABI_LINKER=$ANDROID_NDK_TOOLCHAIN_BIN/armv7a-linux-androideabi28-clang
# cargo build --target armv7-linux-androideabi

# export CC=$ANDROID_NDK_TOOLCHAIN_BIN/x86_64-linux-android28-clang
# export CXX=$ANDROID_NDK_TOOLCHAIN_BIN/x86_64-linux-android28-clang++
# export CARGO_TARGET_X86_64_LINUX_ANDROID_LINKER=$ANDROID_NDK_TOOLCHAIN_BIN/x86_64-linux-android28-clang
# cargo build --target x86_64-linux-android

# export CC=$ANDROID_NDK_TOOLCHAIN_BIN/i686-linux-android28-clang
# export CXX=$ANDROID_NDK_TOOLCHAIN_BIN/i686-linux-android28-clang++
# export CARGO_TARGET_I686_LINUX_ANDROID_LINKER=$ANDROID_NDK_TOOLCHAIN_BIN/i686-linux-android28-clang
# cargo build --target i686-linux-android

export CC=$ANDROID_NDK_TOOLCHAIN_BIN/aarch64-linux-android28-clang
export CXX=$ANDROID_NDK_TOOLCHAIN_BIN/aarch64-linux-android28-clang++
# export CARGO_TARGET_AARCH64_LINUX_ANDROID_AR=$ANDROID_NDK_TOOLCHAIN_BIN/llvm-ar
export CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER=$ANDROID_NDK_TOOLCHAIN_BIN/aarch64-linux-android28-clang
cargo build --target aarch64-linux-android

mkdir -p ../uniffi/src/main/jniLibs/arm64-v8a
cp target/aarch64-linux-android/debug/libuniffi_ruslin.so ../uniffi/src/main/jniLibs/arm64-v8a

uniffi-bindgen generate src/ruslin.udl --language kotlin --out-dir ../uniffi/src/main/java
