/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal;

import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@lombok.Builder
public final class Processing<T extends Element> {

    @lombok.Singular
    private final List<Check<? super T>> checks;

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, ProcessingEnvironment env) {
        if (roundEnv.processingOver()) {
            return false;
        }
        Processors.streamOf(annotations, roundEnv)
                .map(element -> (T) element)
                .forEach(element -> checks.forEach(check -> check.check(env, element)));
        return true;
    }
}
