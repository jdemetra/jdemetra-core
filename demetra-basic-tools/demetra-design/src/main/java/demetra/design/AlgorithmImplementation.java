/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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
 *
 * @author Jean Palate
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface AlgorithmImplementation {

    /**
     *
     */
    public static enum Feature {

        /**
         * Fast implementation
         */
        Fast,
        /**
         * Robust implementation
         */
        Robust,
        /**
         * Balanced between fast and robust
         */
        Balanced,
        /**
         * Should not be used
         */
        Deprecated,
        /**
         * Developed for legacy code
         */
        Legacy,
        /**
         * No specific feature
         */
        None;
    }

    public Class<?> algorithm();

    public Feature feature() default Feature.None;
    
    public int position() default 2147483647;

    public String[] supersedes() default {};

    public String path() default "";
}
