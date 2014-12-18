/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.design;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Flyweight is a software design pattern that minimizes memory use by sharing
 * as much data as possible with other similar objects; it is a way to use
 * objects in large numbers when a simple repeated representation would use an
 * unacceptable amount of memory. Often some parts of the object state can be
 * shared, and it is common practice to hold them in external data structures
 * and pass them to the flyweight objects temporarily when they are used.
 *
 * @see <a
 * href="http://en.wikipedia.org/wiki/Flyweight_pattern">http://en.wikipedia.org/wiki/Flyweight_pattern</a>
 * @author Philippe Charles
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface FlyweightPattern {
}
