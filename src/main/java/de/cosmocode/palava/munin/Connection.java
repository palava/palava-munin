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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * A munin jmx connection.
 * 
 * @author Tobias Sarnowski
 */
public class Connection {
    
    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    private String url;
    private String user;
    private String password;
    private JMXConnector connector;
    private MBeanServerConnection connection;

    public Connection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Connects this connection.
     *
     * @since 1.0
     * @throws IOException if connect failed
     */
    public void connect() throws IOException {
        Map<String, Object> environment = null;
        LOG.info("Connecting {}", this);
        if (user != null && password != null) {
            environment = new HashMap<String, Object>();
            // Which of these are needed? Documentation is unclear ...
            environment.put(JMXConnector.CREDENTIALS, new String[] {user, password});
            environment.put(USERNAME_KEY, user);
            environment.put(PASSWORD_KEY, password);
        }

        final JMXServiceURL jmxUrl = new JMXServiceURL(url);
        connector = JMXConnectorFactory.connect(jmxUrl, environment);
        connection = connector.getMBeanServerConnection();
    }

    /**
     * Queries values for the specified config.
     *
     * @since 1.0
     * @param config the config
     * @return found values
     * @throws JMException if querying failed
     * @throws IOException if querying failed
     */
    public Map<String, FormattedValue> query(ConfigurationFile config) throws JMException, IOException {
        LOG.info("Querying MBeans for {} statistic...", config.getStatistic().getTitle());
        final Map<String, FormattedValue> results = Maps.newHashMap();
        for (ConfiguredGraph graph : config.getStatistic().getGraphs()) {
            LOG.debug("Getting attribute {} of object {}...", graph.getJmxAttributeName(), graph.getJmxObjectName());
            final Object value = connection.getAttribute(graph.getJmxObjectName(), graph.getJmxAttributeName());
            LOG.trace("Got {}", value);
            setResult(results, graph.getTitle(), value, graph.getJmxAttributeKey());
        }
        return results;
    }

    /**
     * Queries all values.
     *
     * @since 1.0
     * @return all found values
     * @throws IOException if querying failed
     * @throws JMException if querying failed
     */
    public Map<String, FormattedValue> queryAll() throws IOException, JMException {
        LOG.info("Querying all MBeans and their attributes...");
        final Map<String, FormattedValue> results = Maps.newHashMap();
        final Set<ObjectName> mbeans = connection.queryNames(null, null);
        for (ObjectName name : mbeans) {
            final MBeanInfo mbeanInfo = connection.getMBeanInfo(name);
            final List<String> attributeNames = new ArrayList<String>();
            for (MBeanAttributeInfo attributeInfo : mbeanInfo.getAttributes()) {
                attributeNames.add(attributeInfo.getName());
            }

            final AttributeList attributes = connection.getAttributes(name, attributeNames.toArray(new String[]{}));
            for (Attribute attribute : attributes.asList()) {
                setResult(results, name.getCanonicalName() + "%" + attribute.getName(), attribute.getValue(), null);
            }
        }
        return results;
    }

    private void setResult(Map<String, FormattedValue> results, String title, Object value, String key) {
        if (value instanceof CompositeDataSupport) {
            final CompositeDataSupport cds = (CompositeDataSupport) value;
            if (key != null) {
                results.put(title, new FormattedValue(cds.get(key)));
            } else {
                for (String k : cds.getCompositeType().keySet()) {
                    results.put(title + "." + k, new FormattedValue(cds.get(k)));
                }
            }
        } else {
            results.put(title, new FormattedValue(value));
        }
    }

    /**
     * Disconnects and closes this connection.
     *
     * @since 1.0
     * @throws IOException if disconnect failed
     */
    public void disconnect() throws IOException {
        LOG.trace("Disconnecting {}", this);
        connector.close();
    }

    @Override
    public String toString() {
        return "Connection [url=" + url + ", user=" + user + "]";
    }

}
