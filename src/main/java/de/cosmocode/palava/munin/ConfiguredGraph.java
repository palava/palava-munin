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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Tobias Sarnowski
 */
public class ConfiguredGraph {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguredGraph.class);


    private String title;

    private String label;

    private ObjectName jmxObjectName;

    private String jmxAttributeName;


    public ConfiguredGraph(String graph_title, Map<String, String> options) throws MalformedObjectNameException {
        title = graph_title;

        // find options for me
        Map<String,String> goptions = Maps.newHashMap();
        for (Map.Entry<String,String> entry: options.entrySet()) {
            int idx;
            String key = entry.getKey();
            if ((idx = key.indexOf(".")) > 0) {
                String major = key.substring(0, idx);
                if (major.equals(title)) {
                    String minor = key.substring(idx + 1);
                    goptions.put(minor, entry.getValue());
                }
            }
        }

        // get them
        label = Preconditions.checkNotNull(goptions.get("label"), "label");
        jmxObjectName = new ObjectName(Preconditions.checkNotNull(goptions.get("jmxObjectName"), "jmxObjectName"));
        jmxAttributeName = Preconditions.checkNotNull(goptions.get("jmxAttributeName"), "jmxAttributeName");
    }

    public String getTitle() {
        return title;
    }

    public String getLabel() {
        return label;
    }

    public ObjectName getJmxObjectName() {
        return jmxObjectName;
    }

    public String getJmxAttributeName() {
        return jmxAttributeName;
    }

    public String dump() {
        String out = title + ".label " + label + "\n";
        out = out + title + ".jmxObjectName " + jmxObjectName + "\n";
        out = out + title + ".jmxAttributeName " + jmxAttributeName + "\n";
        return out;
    }
}