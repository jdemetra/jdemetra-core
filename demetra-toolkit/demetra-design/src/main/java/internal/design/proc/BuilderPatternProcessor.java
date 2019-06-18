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
package internal.design.proc;

import demetra.design.BuilderPattern;
import internal.proc.Check;
import internal.proc.Processing;
import static internal.proc.Processors.*;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("demetra.design.BuilderPattern")
public final class BuilderPatternProcessor extends AbstractProcessor {

    private final Processing<TypeElement> processing = Processing
            .<TypeElement>builder()
            .check(HAS_BUILD_METHOD)
            .build();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return processing.process(annotations, roundEnv, processingEnv);
    }

    private static final Check<TypeElement> HAS_BUILD_METHOD = Check.of(BuilderPatternProcessor::hasBuildMethod, "Cannot find build method in '%s'");

    private static boolean hasBuildMethod(TypeElement e) {
        BuilderPattern annotation = e.getAnnotation(BuilderPattern.class);
        return e.getEnclosedElements().stream().anyMatch(o -> hasBuildMethodOn(o, annotation));
    }

    private static boolean hasBuildMethodOn(Element e, BuilderPattern annotation) {
        return isMethodWithName(e, annotation.buildMethodName())
                && isMethodWithoutParameter(e)
                && isMethodWithReturnInstanceOf(e, annotation::value);
    }
}
