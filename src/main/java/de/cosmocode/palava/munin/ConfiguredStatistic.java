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

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.MalformedObjectNameException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A configured, dumpable statistic.
 * 
 * @author Tobias Sarnowski
 */
public class ConfiguredStatistic implements Dumpable {

    private final String title;

    private final List<ConfiguredGraph> graphs = Lists.newArrayList();

    private final Map<String, String> unused = Maps.newHashMap();
    private final String app;

    public ConfiguredStatistic(String app, Map<String, String> options) throws MalformedObjectNameException {
        for (Map.Entry<String, String> entry : options.entrySet()) {
            if (entry.getKey().startsWith("graph_")) {
                unused.put(entry.getKey(), entry.getValue());
            }
        }

        this.app = Preconditions.checkNotNull(app, "app");
        unused.remove("graph_category");

        title = Preconditions.checkNotNull(options.get("graph_title"), "graph_title");
        unused.remove("graph_title");

        final String order = Preconditions.checkNotNull(options.get("graph_order"), "graph_order");
        unused.remove("graph_order");

        final StringTokenizer tokens = new StringTokenizer(order, " ");
        while (tokens.hasMoreTokens()) {
            final String graphTitle = tokens.nextToken();
            graphs.add(new ConfiguredGraph(graphTitle, options));
        }
    }


    public String getTitle() {
        return title;
    }

    public String getApp() {
        return app;
    }

    public List<ConfiguredGraph> getGraphs() {
        return graphs;
    }

    @Override
    public String dump() {
        String out = "graph_title " + title + "\n";
        out += "graph_category " + app + "\n";

        String order = null;
        for (ConfiguredGraph graph : getGraphs()) {
            if (order == null) {
                order = graph.getTitle();
            } else {
                order = order + " " + graph.getTitle();
            }
        }
        out += "graph_order " + order + "\n";

        for (Map.Entry<String, String> entry : unused.entrySet()) {
            out += entry.getKey() + " " + entry.getValue() + "\n";
        }

        for (ConfiguredGraph graph : getGraphs()) {
            out += graph.dump();
        }
        return out;
    }
}
