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

import java.io.File;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tobias Sarnowski
 */
final class ConsoleOptions {
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleOptions.class);

    @Option(name = "--app", required = true, usage = "Unique name of the monitored application")
    private String app;

    @Option(name = "--url", required = true, usage = "The JMX listener to use.")
    private String url;

    @Option(name = "--user", required = false, usage = "The user to authenticate.")
    private String user;

    @Option(name = "--password", required = false, usage = "The password to authenticate.")
    private String password;

    @Option(name = "--conf", required = false, usage = "The configuration file to use for statistic generation.")
    private File conf;

    @Option(name = "--log", required = false, usage = "Default logging is WARN, can be set to ERROR, WARN, INFO, DEBUG and TRACE.")
    private String log;

    @Option(name = "--dump-config", required = false, usage = "Dumps the given configuration file")
    private boolean dumpConfig;

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public File getConf() {
        return conf;
    }

    public String getLog() {
        return log;
    }

    public boolean isDumpConfig() {
        return dumpConfig;
    }

    public String getApp() {
        return app;
    }
}