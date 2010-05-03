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

#
# Wildcard-plugin to monitor Java JMX (http://java.sun.com/jmx) attributes.
# To monitor a # specific set of JMX attributes, 
# link <config_name> to this file. E.g.
#
#    ln -s /opt/palava-munin/bin/palava-munin.sh /etc/munin/plugins/myapp_java_threads
#

# ...will monitor Java thread count, assuming java_threads.conf file is located in
# /opt/palava-munin/conf folder.
#
# For Java process to be monitored, it must expose JMX remote interface.
# With Java 1.5 it can be done by adding parameters as:
#
# -Dcom.sun.management.jmxremote.port=<PORT> -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false
#
# By default, the plugin monitors localhost on <PORT> = 1616 using URL
# service:jmx:rmi:///jndi/rmi://localhost:1616/jmxrmi
# with no authentication credentials
# It can be changed by specifying parameters in  /etc/munin/plugin-conf.d/munin-node
#
# [myapp_*]
# env.jmxurl service:jmx:rmi:///jndi/rmi://localhost:1616/jmxrmi
# env.jmxuser monitorRole
# env.jmxpass QED
#
# Read more on JMX configuring at http://java.sun.com/j2se/1.5.0/docs/guide/management/agent.html
#

# who are we?
ME="$(readlink $0)"
if [ -z "$ME" ]; then
    echo "You cannot call me directly" >&2
    exit 1
fi

# and what do we want?
APPNAME="$(basename $0 | cut -d'_' -f1)"
CONFIGNAME="$(basename $0 | cut -d'_' -f2-).conf"

# we're in bin/ so go one up
cd $(dirname $(dirname $ME))


# settings from munin
if [ -z "$jmxurl" ]; then
    SERVICE=service:jmx:rmi:///jndi/rmi://localhost:1616/jmxrmi
else
    SERVICE="$jmxurl"
fi

if [ "$jmxuser" != "" ]; then
    CREDS="--user=$jmxuser --password=$jmxpass"
fi

# this really should be there
if [ ! -r "conf/$CONFIGNAME" ]; then
    echo "Configuration file not available"
    exit 1
fi

if [ "$1" = "config" ]; then
    DUMP_CONFIG="--dump-config"
else
    DUMP_CONFIG=
fi

# how to execute java
java -cp 'lib/*' de.cosmocode.palava.munin.Main --app $APPNAME --url $SERVICE $CREDS --conf conf/$CONFIGNAME $DUMP_CONFIG
exit $?