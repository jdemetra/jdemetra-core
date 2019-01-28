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

package demetra.design;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that a class is a primitive-friendly drop-in replacement of a
 * generic class.<p>Generics in Java don't handle primivites. You can't write
 * <code>List&lt;int&gt;</code>. Instead you have to write
 * <code>List&lt;Integer&gt;</code>.<br>This leads to autoboxing (int &lt;-&gt;
 * Integer) and therefore some overhead. To avoid this problem, we use some
 * specialize classes.
 *
 * @author Philippe Charles
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface PrimitiveReplacementOf {

    Class<?> generic();

    Class<?> primitive();
}
