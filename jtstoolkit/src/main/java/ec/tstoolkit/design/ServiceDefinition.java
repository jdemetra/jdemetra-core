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
import java.util.ServiceLoader;

/**
 * Defines an interface as being used as a service.<br>
 *
 * @see ServiceLoader
 * @author Philippe Charles
 */
@Deprecated
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ServiceDefinition {

    /**
     * Specifies if this service has an optional position used to register this
     * service relative to others. Lower-numbered services are returned in the
     * lookup result first. Services with no specified position are returned
     * last.
     */
    boolean hasPosition() default false;

    boolean isSingleton() default false;
}
