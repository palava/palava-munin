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
 * An object which uses some sophisticated formatting action
 * in {@link #toString()}.
 * 
 * @author Tobias Sarnowski
 */
public class FormattedValue {

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
            final NumberFormat format = NumberFormat.getInstance(Locale.US);
            format.setMaximumFractionDigits(2);
            format.setGroupingUsed(false);
            return format.format(value);
        } else if (value instanceof Object[]) {
            final StringBuilder b = new StringBuilder();
            for (Object obj : (Object[]) value) {
                final FormattedValue v = new FormattedValue(obj);
                if (b.length() == 0) {
                    b.append(v);
                } else {
                    b.append(" ").append(v);
                }
            }
            return b.toString();
        } else {
            return value.toString();
        }
    }

    public Object getValue() {
        return value;
    }
    
}
