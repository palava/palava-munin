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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Tobias Sarnowski
 */
public class ConfiguredStatistic {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguredStatistic.class);


    private String title;

    private String vlabel;

    private String category;

    private List<ConfiguredGraph> graphs = new ArrayList<ConfiguredGraph>();


    public ConfiguredStatistic(String app, Map<String, String> options) throws MalformedObjectNameException {
        title = Preconditions.checkNotNull(options.get("graph_title"), "graph_title");
        vlabel = Preconditions.checkNotNull(options.get("graph_vlabel"), "graph_vlabel");
        category = Preconditions.checkNotNull(options.get("graph_category"), "graph_category");

        category = category + ": " + app;

        String order = Preconditions.checkNotNull(options.get("graph_order"), "graph_order");

        StringTokenizer tokens = new StringTokenizer(order, " ");
        while (tokens.hasMoreTokens()) {
            String graph_title = tokens.nextToken();
            graphs.add(new ConfiguredGraph(graph_title, options));
        }
    }


    public String getTitle() {
        return title;
    }

    public String getVLabel() {
        return vlabel;
    }

    public String getCategory() {
        return category;
    }

    public List<ConfiguredGraph> getGraphs() {
        return graphs;
    }

    public String dump() {
        String out = "graph_title " + title + "\n";
        out = out + "graph_vlabel " + vlabel + "\n";
        out = out + "graph_category " + category + "\n";

        String order = null;
        for (ConfiguredGraph graph: getGraphs()) {
            if (order == null) {
                order = graph.getTitle();
            } else {
                order = order + " " + graph.getTitle();
            }
        }
        out = out + "graph_order " + order + "\n";
        out = out + "\n";

        for (ConfiguredGraph graph: getGraphs()) {
            out = out + graph.dump();
            out = out + "\n";
        }
        return out;
    }
}