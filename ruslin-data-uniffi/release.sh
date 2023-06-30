#!/bin/bash

set -e

GIT_COMMIT_HASH=$(git rev-parse --verify HEAD | tr -d '\n')

# After upgrading to 1.71.0, switch to `sparse`.
export CARGO_REGISTRIES_CRATES_IO_PROTOCOL=git

export RUSTFLAGS="--cfg uuid_unstable"
export RUSTFLAGS="$RUSTFLAGS --remap-path-prefix=$HOME/.cargo/=/.cargo/"
export RUSTFLAGS="$RUSTFLAGS --remap-path-prefix=$PWD/=/ruslin-data-uniffi/$GIT_COMMIT_HASH/"

# This workaround should be removed 
# after the https://github.com/llvm/llvm-project/commit/95dcaef00379e893dabc61cf598fe51c9d03414e change is merged into the NDK
OS_RELEASE_ID=$(grep -oP '(?<=^ID=).+' /etc/os-release | tr -d '"')
if [ "$OS_RELEASE_ID" = "debian" ]; then
    export RUSTFLAGS="$RUSTFLAGS -C link-args=-Wl,--hash-style=gnu"
fi

echo "RUSTFLAGS: $RUSTFLAGS"
# https://github.com/briansmith/ring/issues/715 ?
# export CFLAGS="-fdebug-prefix-map=$(pwd)=." 

ANDROID_NDK_TOOLCHAIN_BIN=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin
export AR=$ANDROID_NDK_TOOLCHAIN_BIN/llvm-ar

ANDROID_ABI=$1

export BINDGEN_EXTRA_CLANG_ARGS="--sysroot=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/sysroot"

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
cargo fetch
cargo build --target $RUST_TARGET --verbose --release --frozen --locked 
mkdir -p ../uniffi/src/main/jniLibs/$ANDROID_ABI
cp target/$RUST_TARGET/release/libuniffi_ruslin.so ../uniffi/src/main/jniLibs/$ANDROID_ABI
