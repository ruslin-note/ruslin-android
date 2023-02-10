#!/bin/bash

set -e

find -L $ANDROID_NDK_ROOT -name libunwind.a -execdir sh -c 'echo "INPUT(-lunwind)" > libgcc.a' \;

ANDROID_NDK_TOOLCHAIN_BIN=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin
export AR=$ANDROID_NDK_TOOLCHAIN_BIN/llvm-ar

ANDROID_ABI=$1

case "$ANDROID_ABI" in
arm64-v8a)
    RUST_TARGET="aarch64-linux-android"
    export CC=$ANDROID_NDK_TOOLCHAIN_BIN/aarch64-linux-android28-clang
    export CXX=$ANDROID_NDK_TOOLCHAIN_BIN/aarch64-linux-android28-clang++
    export CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER=$ANDROID_NDK_TOOLCHAIN_BIN/aarch64-linux-android28-clang
    ;;
armeabi-v7a)
    RUST_TARGET="armv7-linux-androideabi"
    export CC=$ANDROID_NDK_TOOLCHAIN_BIN/armv7a-linux-androideabi28-clang
    export CXX=$ANDROID_NDK_TOOLCHAIN_BIN/armv7a-linux-androideabi28-clang++
    export CARGO_TARGET_ARMV7_LINUX_ANDROIDEABI_LINKER=$ANDROID_NDK_TOOLCHAIN_BIN/armv7a-linux-androideabi28-clang
    ;;
x86_64)
    RUST_TARGET="x86_64-linux-android"
    export CC=$ANDROID_NDK_TOOLCHAIN_BIN/x86_64-linux-android28-clang
    export CXX=$ANDROID_NDK_TOOLCHAIN_BIN/x86_64-linux-android28-clang++
    export CARGO_TARGET_X86_64_LINUX_ANDROID_LINKER=$ANDROID_NDK_TOOLCHAIN_BIN/x86_64-linux-android28-clang
    ;;
x86)
    RUST_TARGET="i686-linux-android"
    export CC=$ANDROID_NDK_TOOLCHAIN_BIN/i686-linux-android28-clang
    export CXX=$ANDROID_NDK_TOOLCHAIN_BIN/i686-linux-android28-clang++
    export CARGO_TARGET_I686_LINUX_ANDROID_LINKER=$ANDROID_NDK_TOOLCHAIN_BIN/i686-linux-android28-clang
    ;;
*)
    echo "Unsupported"
    exit 1
esac

echo "Rust target: $RUST_TARGET"

cargo build --target $RUST_TARGET --verbose --release
mkdir -p ../uniffi/src/main/jniLibs/$ANDROID_ABI
cp target/$RUST_TARGET/release/libuniffi_ruslin.so ../uniffi/src/main/jniLibs/$ANDROID_ABI
