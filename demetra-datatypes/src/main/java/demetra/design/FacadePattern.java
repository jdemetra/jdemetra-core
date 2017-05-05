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
 * Facade is a software design pattern that provides a simplified interface to a
 * larger body of code, such as a class library. A facade can:
 * <ul>
 * <li>make a software library easier to use, understand and test, since the
 * facade has convenient methods for common tasks;
 * <li>make the library more readable, for the same reason;
 * <li>reduce dependencies of outside code on the inner workings of a library,
 * since most code uses the facade, thus allowing more flexibility in developing
 * the system;
 * <li>wrap a poorly designed collection of APIs with a single well-designed API
 * (as per task needs).
 * </ul>
 *
 * @see <a
 * href="http://en.wikipedia.org/wiki/Facade_pattern">http://en.wikipedia.org/wiki/Facade_pattern</a>
 * @author Philippe Charles
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface FacadePattern {
}
