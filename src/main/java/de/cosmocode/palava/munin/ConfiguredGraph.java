/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.munin;

import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * A configured, dumpable graph.
 * 
 * @author Tobias Sarnowski
 */
public class ConfiguredGraph implements Dumpable {

    private String title;

    private ObjectName jmxObjectName;
    private String jmxAttributeName;
    private String jmxAttributeKey;

    private Map<String, String> unused;

    public ConfiguredGraph(String title, Map<String, String> options) throws MalformedObjectNameException {
        this.title = title;

        // find options for me
        final Map<String, String> goptions = Maps.newHashMap();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            final String key = entry.getKey();
            final int idx = key.indexOf(".");
            
            if (idx > 0) {
                final String major = key.substring(0, idx);
                if (major.equals(title)) {
                    final String minor = key.substring(idx + 1);
                    goptions.put(minor, entry.getValue());
                }
            }
        }
        unused = goptions;

        // get them
        jmxObjectName = new ObjectName(Preconditions.checkNotNull(goptions.get("jmxObjectName"), "jmxObjectName"));
        unused.remove("jmxObjectName");

        jmxAttributeName = Preconditions.checkNotNull(goptions.get("jmxAttributeName"), "jmxAttributeName");
        unused.remove("jmxAttributeName");

        // can be unset
        jmxAttributeKey = goptions.get("jmxAttributeKey");
        
        unused.remove("jmxAttributeKey");
    }

    public String getTitle() {
        return title;
    }

    public ObjectName getJmxObjectName() {
        return jmxObjectName;
    }

    public String getJmxAttributeName() {
        return jmxAttributeName;
    }

    public String getJmxAttributeKey() {
        return jmxAttributeKey;
    }

    @Override
    public String dump() {
        final StringBuilder b = new StringBuilder();
        for (Map.Entry<String, String> entry : unused.entrySet()) {
            b.append(title);
            b.append(".");
            b.append(entry.getKey());
            b.append(" ");
            b.append(entry.getValue());
            b.append("\n");
        }
        return b.toString();
    }
   
}
