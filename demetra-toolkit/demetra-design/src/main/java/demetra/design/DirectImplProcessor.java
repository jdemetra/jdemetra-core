/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.design;

import java.util.Arrays;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("demetra.design.DirectImpl")
public final class DirectImplProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        annotations.stream()
                .flatMap(o -> roundEnv.getElementsAnnotatedWith(o).stream())
                .forEach(o -> checkElement((TypeElement) o));

        return true;
    }

    void checkElement(TypeElement e) {
        if (!e.getModifiers().containsAll(Arrays.asList(Modifier.FINAL))) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Class '%s' must be final", e.getQualifiedName()));
        }
        if (!e.getSuperclass().toString().equals(Object.class.getName())) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Class '%s' may not extend another class", e.getQualifiedName()));
        }
        if (e.getEnclosedElements().stream().anyMatch(o -> isVariableNotStaticButPublic(o))) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("Class '%s' may not contain public vars", e.getQualifiedName()));
        }
    }

    private static boolean isVariableNotStaticButPublic(Element e) {
        Set<Modifier> modifiers = e.getModifiers();
        return e instanceof VariableElement
                && !modifiers.contains(Modifier.STATIC)
                && modifiers.contains(Modifier.PUBLIC);
    }
}
