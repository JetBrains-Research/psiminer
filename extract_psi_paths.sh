#!/usr/bin/env bash

if [ $# -ne "3" ]; then
    echo "usage: extract_psi_paths <path to dataset> <path to output folder> <path to json config>"
    exit 1
fi

# https://stackoverflow.com/a/246128
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
if uname -s | grep -iq cygwin ; then
    DIR=$(cygpath -w "$DIR")
    PWD=$(cygpath -w "$PWD")
fi

"$DIR/gradlew" --stop
"$DIR/gradlew" clean
"$DIR/gradlew" -p "$DIR" extractPSIPaths -Pdataset="$PWD/$1" -Poutput="$PWD/$2" -Pconfig="$PWD/$3"
