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
 * The visible state of objects of this class doesn't change after their
 * creation.
 * Those objects should not be cloned or copied.
 * They are thread safe
 * 
 * General rules:
 * - the object is fully initialized in the constructor (fields should be final)
 * - the class should be final
 * 
 * Special cases: 
 * - the initialization of some fields could be delayed, for performances issues.
 * - in that case, the internal code should preserve the integrity of the object in a multi-threaded context.
 * 
 * @author Jean Palate
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Immutable {
}
