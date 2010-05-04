#!/bin/sh
#
# Copyright 2010 CosmoCode GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# get absolute path to plugin dir
cd $(dirname $0)/..
DIR=$(pwd)

# basic parameters
if [ ! -z "$1" ]; then
    APP=$1
    shift
fi
if [ ! -z "$1" ]; then
    ETC=$1
    shift
fi

if [ -z "$APP" ] || [ -z "$ETC" ] || [ ! -d "$ETC" ]; then
    echo "Usage: $0 <appname> <destination> [<group1> [<group2> [...]]]" >&2
    echo >&2
    echo "Example:" >&2
    echo "   $0 MyApp /etc/munin/plugins" >&2
    echo "   $0 MyApp /etc/munin/plugins java palava" >&2
    echo >&2
    exit 1
fi

# clean up old links
rm -f ${ETC}/${APP}_*

# set up links
for conf in $(ls conf); do
    key=$(echo $conf | sed 's/\.conf$//g')

    do=true
    if [ ! -z "$*" ]; then
        do=false
        for group in $*; do
            if [ ! -z "$(echo $key | grep "^${group}_")" ]; then
                do=true
                break
            fi
        done
    fi

    [ $do = true ] && ln -sv $DIR/bin/palava-munin.sh ${ETC}/${APP}_${key}

done