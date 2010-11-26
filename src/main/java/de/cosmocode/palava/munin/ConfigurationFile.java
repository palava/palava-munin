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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MalformedObjectNameException;

import com.google.common.base.Preconditions;

/**
 * Reader and value object for munin configuration files.
 * 
 * Format of the configuration file is:
 * <key> <value>
 *
 * @author Tobias Sarnowski
 */
public class ConfigurationFile {

    private File file;

    private Map<String, String> options = new HashMap<String, String>();

    private ConfiguredStatistic statistic;

    public ConfigurationFile(String app, File file) throws IOException {
        this.file = file;

        // parse it to map
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            parse(line.trim());
        }

        // now do it
        try {
            statistic = new ConfiguredStatistic(app, options);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void parse(String line) {
        if (line.length() == 0) {
            return;
        }
        
        final int index = line.indexOf(" ");
        Preconditions.checkArgument(index >= 0, "invalid configuration line:  %s", line);
        
        final String key = line.substring(0, index);
        final String value = line.substring(index + 1);

        options.put(key, value);
    }

    public File getFile() {
        return file;
    }

    public ConfiguredStatistic getStatistic() {
        return statistic;
    }

    public Map<String, String> getOptions() {
        return options;
    }
    
}
