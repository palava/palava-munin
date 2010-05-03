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
import java.text.NumberFormat;
import java.util.*;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import de.cosmocode.palava.munin.Configuration.FieldProperties;

/**
 * 
 * JMXQuery is used for local or remote request of JMX attributes It requires
 * JRE 1.5 to be used for compilation and execution. Look method main for
 * description how it can be invoked.
 * 
 */
public class JMXQuery
{
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";
	
	public static final String USAGE = "Usage of program is:\n"
        + "java -cp jmxquery.jar de.cosmocode.palava.munin.JMXQuery --url=<URL> [--user=<username> --pass=<password>] [--conf=<config file> [config]]\n"
        + ", where <URL> is a JMX URL, for example: service:jmx:rmi:///jndi/rmi://HOST:PORT/jmxrmi\n"
        + "When invoked with the config file (see examples folder) - operates as Munin plugin with the provided configuration\n"
        + "Without options just fetches all JMX attributes using provided URL";

	private String url;
	private String username;
	private String password;
	private JMXConnector connector;
	private MBeanServerConnection connection;
	private Configuration config;

	public JMXQuery(String url)
	{
		this(url, null, null);
	}
	
	public JMXQuery(String url, String username, String password)
	{
		this.url = url;
		this.username = username;
		this.password = password;
	}

	private void connect() throws IOException
	{
		Map<String, Object> environment = null;
		if (username != null && password != null)
		{
            environment = new HashMap<String, Object>();
            // Which of these are needed? Documentation is unclear ...
            environment.put(JMXConnector.CREDENTIALS, new String[] {username, password});
            environment.put(USERNAME_KEY, username);
            environment.put(PASSWORD_KEY, password);
		}
		
		JMXServiceURL jmxUrl = new JMXServiceURL(url);
		connector = JMXConnectorFactory.connect(jmxUrl, environment);
		connection = connector.getMBeanServerConnection();		
	}

	private void list() throws IOException, InstanceNotFoundException, IntrospectionException, ReflectionException
	{
		if (config == null)
		{
			//listAll();
		}
		else
		{
			listConfig();
		}
	}

	private void listConfig()
	{
		for (FieldProperties field : config.getFields())
		{
			try
			{
				Object value = connection.getAttribute(field.getJmxObjectName(), field.getJmxAttributeName());
				output(field.getFieldname(), value, field.getJmxAttributeKey());
			} 
			catch (Exception e)
			{
				System.err.println("Fail to output " + field);
				e.printStackTrace();
			}
		}
	}

	private void output(String name, Object attr, String key)
	{
		if (attr instanceof CompositeDataSupport)
		{
			CompositeDataSupport cds = (CompositeDataSupport) attr;
			if (key == null)
			{
				throw new IllegalArgumentException("Key is null for composed data " + name);
			}
			System.out.println(name + ".value " + format(cds.get(key)));
		} 
		else
		{
			System.out.println(name + ".value " + format(attr));
		}
	}

	private void output(String name, Object attr)
	{
		if (attr instanceof CompositeDataSupport)
		{
			CompositeDataSupport cds = (CompositeDataSupport) attr;
			for (Iterator it = cds.getCompositeType().keySet().iterator(); it.hasNext();)
			{
				String key = it.next().toString();
				System.out.println(name + "." + key + ".value " + format(cds.get(key)));
			}
		}
		else
		{
			System.out.println(name + ".value " + format(attr));
		}
	}

	@SuppressWarnings("unchecked")
	private void listAll() throws IOException, JMException
	{

	}

	private String format(Object value)
	{
		if (value == null)
		{
			return null;
		}
		else if (value instanceof String)
		{
			return (String) value;
		}
		else if (value instanceof Number)
		{
			NumberFormat f = NumberFormat.getInstance();
			f.setMaximumFractionDigits(2);
			f.setGroupingUsed(false);
			return f.format(value);
		} 
		else if (value instanceof Object[])
		{
			return Arrays.toString((Object[]) value);
		}
		return value.toString();
	}

	private void disconnect() throws IOException
	{
		connector.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		int arglen = args.length;
		
		if (arglen < 1)
		{
			System.err.println(USAGE);
			System.exit(1);
		}

		String url = null;
		String user = null;
		String pass = null;
		String config_file = null;
		boolean toconfig = false;
		
		for (int i = 0; i < arglen; i++)
		{
			if (args[i].startsWith("--url="))
			{
				url = args[i].substring(6);
			}
			else if (args[i].startsWith("--user="))
			{
				user = args[i].substring(7);
			}
			else if (args[i].startsWith("--pass="))
			{
				pass = args[i].substring(7);
			}
			else if (args[i].startsWith("--conf="))
			{
				config_file = args[i].substring(7);
			}
			else if (args[i].equals("config"))
			{
				toconfig = true;
			}
		}

		if (url == null || (user != null && pass == null) || (user == null && pass != null) || (config_file == null && toconfig))
		{
			System.err.println(USAGE);
			System.exit(1);
		}

		if (toconfig)
		{
			try
			{
				Configuration.parse(config_file).report(System.out);
			} 
			catch (Exception e)
			{
				System.err.println(e.getMessage() + " reading " + config_file);
				System.exit(1);
			}
		} 
		else
		{
			JMXQuery query = new JMXQuery(url, user, pass);
			try
			{
				query.connect();
				if (config_file != null)
				{
					query.setConfig(Configuration.parse(config_file));
				}
				query.list();
			} 
			catch (Exception ex)
			{
				System.err.println(ex.getMessage() + " querying " + url);
				ex.printStackTrace();
				System.exit(1);
			} 
			finally
			{
				try
				{
					query.disconnect();
				} 
				catch (IOException e)
				{
					System.err.println(e.getMessage() + " closing " + url);
				}
			}
		}

	}

	private void setConfig(Configuration configuration)
	{
		this.config = configuration;
	}

	public Configuration getConfig()
	{
		return config;
	}

}
