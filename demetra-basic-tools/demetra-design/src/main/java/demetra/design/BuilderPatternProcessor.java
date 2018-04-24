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
package demetra.design;

import internal.Processors;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("demetra.design.BuilderPattern")
public final class BuilderPatternProcessor extends AbstractProcessor {

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

    private void checkElement(TypeElement e) {
        BuilderPattern annotation = e.getAnnotation(BuilderPattern.class);

        if (e.getEnclosedElements().stream().noneMatch(o -> hasBuildMethod(o, annotation))) {
            reportError("Cannot find build method in '%s'", e.getQualifiedName());
        }
    }

    private boolean hasBuildMethod(Element e, BuilderPattern annotation) {
        return Processors.isMethodWithName(e, annotation.buildMethodName())
                && Processors.isMethodWithoutParameter(e)
                && Processors.isMethodWithReturnInstanceOf(e, annotation::value);
    }

    private void reportError(String format, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
    }
}
