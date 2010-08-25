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

import java.text.NumberFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tobias Sarnowski
 */
public class FormattedValue {
    private static final Logger LOG = LoggerFactory.getLogger(FormattedValue.class);

    private Object value;

    public FormattedValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (value == null) {
			return "null";

		} else if (value instanceof String) {
			return (String) value;

		} else if (value instanceof Number) {
			NumberFormat f = NumberFormat.getInstance(Locale.US);
			f.setMaximumFractionDigits(2);
			f.setGroupingUsed(false);
			return f.format(value);

		} else if (value instanceof Object[]) {
            String concatinated = null;
			for (Object obj: ((Object[])value)) {
                FormattedValue v = new FormattedValue(obj);
                if (concatinated != null) {
                    concatinated += " " + v.toString();
                } else {
                    concatinated = v.toString();
                }
            }
            return concatinated;

		} else {
		    return value.toString();
        }
    }

    public Object getValue() {
        return value;
    }
}