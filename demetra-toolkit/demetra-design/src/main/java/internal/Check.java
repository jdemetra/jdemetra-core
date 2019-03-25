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

import java.util.Arrays;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface Check<T extends Element> {

    void check(ProcessingEnvironment env, T type);

    static <T extends Element> Check<T> of(Predicate<? super T> condition, String formattedMessage) {
        return (ProcessingEnvironment env, T type) -> {
            if (!condition.test(type)) {
                Processors.error(env, type, formattedMessage);
            }
        };
    }

    static <T extends Element> Check<T> of(BiPredicate<ProcessingEnvironment, ? super T> condition, String formattedMessage) {
        return (ProcessingEnvironment env, T type) -> {
            if (!condition.test(env, type)) {
                Processors.error(env, type, formattedMessage);
            }
        };
    }

    static final Check<Element> IS_PUBLIC = Check.of(Check::isPublic, "'%s' must be public");
    static final Check<Element> IS_FINAL = Check.of(Check::isFinal, "'%s' must be final");

    static final Check<TypeElement> IS_INTERFACE = Check.of(Check::isInterface, "'%s' must be an interface");
    static final Check<TypeElement> DO_NOT_EXTEND_CLASS = Check.of(Check::doNotExtendClass, "'%s' may not extend another class");
    static final Check<TypeElement> DO_NOT_CONTAIN_PUBLIC_VARS = Check.of(Check::doNotContainPublicVars, "'%s' may not contain public vars");

    static boolean isPublic(Element type) {
        return type.getModifiers().containsAll(Arrays.asList(Modifier.PUBLIC));
    }

    static boolean isFinal(Element type) {
        return type.getModifiers().containsAll(Arrays.asList(Modifier.FINAL));
    }

    static boolean isInterface(TypeElement type) {
        return type.getKind().isInterface();
    }

    static boolean doNotExtendClass(TypeElement type) {
        return type.getSuperclass().toString().equals(Object.class.getName());
    }

    static boolean doNotContainPublicVars(TypeElement type) {
        return type.getEnclosedElements().stream().noneMatch(Check::isVariableNotStaticButPublic);
    }

    static boolean isVariableNotStaticButPublic(Element e) {
        Set<Modifier> modifiers = e.getModifiers();
        return e instanceof VariableElement
                && !modifiers.contains(Modifier.STATIC)
                && modifiers.contains(Modifier.PUBLIC);
    }
}
