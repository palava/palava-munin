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
import java.util.Map;

import javax.management.JMException;

import org.apache.log4j.Level;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point.
 * 
 * @author Tobias Sarnowski
 */
public final class Main {
    
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    
    private Main() {
        
    }

    /**
     * Runs this application.
     *
     * @since 1.0
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // parse console parameters
        final ConsoleOptions options = new ConsoleOptions();
        final CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            parser.printUsage(System.err);
            throw new IllegalArgumentException(e);
        }

        // we manipulate log4j directly to support our --log configuration
        final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getRootLogger();
        logger.setLevel(Level.toLevel(options.getLog()));

        // the connection to send
        final Connection connection = new Connection(options.getUrl(), options.getUser(), options.getPassword());
        LOG.debug("Connection to use: {}", connection);

        // load the configuration file if one is given
        final ConfigurationFile config;
        if (options.getConf() != null) {
            LOG.debug("Using configuration file: {}", options.getConf());
            try {
                config = new ConfigurationFile(options.getApp(), options.getConf());
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            LOG.debug("Using no configuration file");
            config = null;
        }

        if (options.isDumpConfig()) {
            // dump the configuration file
            LOG.debug("Dumping configuration file {}", config.getFile());
            System.out.print(config.getStatistic().dump());
        } else {
            try {
                Map<String, FormattedValue> results;
                
                connection.connect();
                
                if (config == null) {
                    results = connection.queryAll();
                } else {
                    results = connection.query(config);
                }
                
                for (Map.Entry<String, FormattedValue> result : results.entrySet()) {
                    System.out.println(result.getKey() + ".value " + result.getValue().toString());
                }
                
                connection.disconnect();
            } catch (JMException e) {
                throw new IllegalStateException(e);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    
}
