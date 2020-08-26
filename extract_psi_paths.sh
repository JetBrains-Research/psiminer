#!/usr/bin/env bash

if [ $# -lt "2" ]; then
    echo "usage: extract_psi_paths <path to dataset> <path to output folder> <additional parsing args>"
    exit 1
fi

# https://stackoverflow.com/a/246128
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
if uname -s | grep -iq cygwin ; then
    DIR=$(cygpath -w "$DIR")
    PWD=$(cygpath -w "$PWD")
fi

if [ $# -ge "3" ]; then
    "$DIR/gradlew" -p "$DIR" extractPSIPaths -Pdataset="$PWD/$1" -Poutput="$PWD/$2" -PparsingArgs="${@:3}"
else
    "$DIR/gradlew" -p "$DIR" extractPSIPaths -Pdataset="$PWD/$1" -Poutput="$PWD/$2"
fi