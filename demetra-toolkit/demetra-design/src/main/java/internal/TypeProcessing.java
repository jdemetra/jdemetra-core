/*
 * Copyright 2018 National Bank of Belgium
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
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 *
 * @author Philippe Charles
 */
@lombok.Builder(builderClassName = "Builder")
public final class TypeProcessing {

    @lombok.Singular
    private final List<Check> checks;

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, ProcessingEnvironment env) {
        if (roundEnv.processingOver()) {
            return false;
        }
        Processors.typeStreamOf(annotations, roundEnv).forEach(type -> checkType(type, env));
        return true;
    }

    private void checkType(TypeElement type, ProcessingEnvironment env) {
        checks.forEach(check -> check.check(env, type));
    }

    public interface Check {

        void check(ProcessingEnvironment env, TypeElement type);

        static Check of(Predicate<? super TypeElement> condition, String formattedMessage) {
            return (env, type) -> {
                if (!condition.test(type)) {
                    error(env, type, formattedMessage);
                }
            };
        }
    }

    public static final Check IS_INTERFACE = Check.of(TypeProcessing::isInterface, "'%s' must be an interface");
    public static final Check IS_PUBLIC = Check.of(TypeProcessing::isPublic, "'%s' must be public");
    public static final Check IS_FINAL = Check.of(TypeProcessing::isFinal, "'%s' must be final");
    public static final Check DO_NOT_EXTEND_CLASS = Check.of(TypeProcessing::doNotExtendClass, "'%s' may not extend another class");
    public static final Check DO_NOT_CONTAIN_PUBLIC_VARS = Check.of(TypeProcessing::doNotContainPublicVars, "'%s' may not contain public vars");

    private static boolean isInterface(TypeElement type) {
        return type.getKind().isInterface();
    }

    private static boolean isPublic(TypeElement type) {
        return type.getModifiers().containsAll(Arrays.asList(Modifier.PUBLIC));
    }

    private static boolean isFinal(TypeElement type) {
        return type.getModifiers().containsAll(Arrays.asList(Modifier.FINAL));
    }

    private static boolean doNotExtendClass(TypeElement type) {
        return type.getSuperclass().toString().equals(Object.class.getName());
    }

    private static boolean doNotContainPublicVars(TypeElement type) {
        return type.getEnclosedElements().stream().noneMatch(TypeProcessing::isVariableNotStaticButPublic);
    }

    private static boolean isVariableNotStaticButPublic(Element e) {
        Set<Modifier> modifiers = e.getModifiers();
        return e instanceof VariableElement
                && !modifiers.contains(Modifier.STATIC)
                && modifiers.contains(Modifier.PUBLIC);
    }

    private static void error(ProcessingEnvironment env, TypeElement type, String formattedMessage) {
        env.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(formattedMessage, type), type);
    }
}
