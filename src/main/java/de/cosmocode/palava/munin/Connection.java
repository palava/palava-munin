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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.*;

/**
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

    public void connect() throws IOException {
        Map<String, Object> environment = null;
        LOG.info("Connecting {}", this);
		if (user != null && password != null)
		{
            environment = new HashMap<String, Object>();
            // Which of these are needed? Documentation is unclear ...
            environment.put(JMXConnector.CREDENTIALS, new String[] {user, password});
            environment.put(USERNAME_KEY, user);
            environment.put(PASSWORD_KEY, password);
        }

		JMXServiceURL jmxUrl = new JMXServiceURL(url);
		connector = JMXConnectorFactory.connect(jmxUrl, environment);
		connection = connector.getMBeanServerConnection();
    }


    public void disconnect() throws IOException {
        LOG.trace("Disconnecting {}", this);
        connector.close();
    }

    public Map<String,Object> query(ConfigurationFile config) throws JMException, IOException {
        LOG.info("Querying MBeans for {} statistic...", config.getStatistic().getTitle());
        Map<String,Object> results = new HashMap<String,Object>();
        for (ConfiguredGraph graph: config.getStatistic().getGraphs()) {
            LOG.debug("Getting attribute {} of object {}...", graph.getJmxAttributeName(), graph.getJmxObjectName());
            Object value = connection.getAttribute(graph.getJmxObjectName(), graph.getJmxAttributeName());
            LOG.trace("Got {}", value);
            results.put(graph.getTitle() + ".value", value);
        }
        return results;
    }

    public Map<String, Object> queryAll() throws IOException, JMException {
        LOG.info("Querying all MBeans and their attributes...");
        Map<String,Object> results = new HashMap<String,Object>();
        Set<ObjectName> mbeans = connection.queryNames(null, null);
		for (ObjectName name : mbeans) {
			MBeanInfo mbeanInfo = connection.getMBeanInfo(name);
            ArrayList<String> attributeNames = new ArrayList<String>();
            for (MBeanAttributeInfo attributeInfo: mbeanInfo.getAttributes()) {
                attributeNames.add(attributeInfo.getName());
            }
            
            AttributeList attributes = connection.getAttributes(name, attributeNames.toArray(new String[]{}));
            for (Attribute attribute: attributes.asList()) {
                results.put(name.getCanonicalName() + " [" + attribute.getName() + "]", attribute.getValue());
            }
		}
        return results;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "url='" + url + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}