#!/bin/bash

set -e

pushd app/build/outputs/mapping/release
zip mapping.zip *.txt
popd
