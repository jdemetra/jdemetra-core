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

import demetra.design.Immutable;
import internal.proc.Check;
import static internal.proc.Check.IS_FINAL;
import internal.proc.Processing;
import static internal.proc.Processors.getNonStaticFields;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("demetra.design.Immutable")
public final class ImmutableProcessor extends AbstractProcessor {

    private final Processing<TypeElement> processing = Processing
            .<TypeElement>builder()
            .check(IS_FINAL)
            .check(ARE_FIELDS_FINAL_OR_LAZY)
            .check(ARE_FIELDS_PRIVATE)
            .check(HAS_LAZY_FIELD_IF_LAZY)
            .build();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return processing.process(annotations, roundEnv, processingEnv);
    }

    private static final Check<TypeElement> ARE_FIELDS_FINAL_OR_LAZY = Check.of(ImmutableProcessor::areFieldsFinalOrLazy, "Fields of '%s' must be final or lazy");
    private static final Check<TypeElement> ARE_FIELDS_PRIVATE = Check.of(ImmutableProcessor::areFieldsPrivate, "Fields of '%s' must be private");
    private static final Check<TypeElement> HAS_LAZY_FIELD_IF_LAZY = Check.of(ImmutableProcessor::hasLazyFieldIfLazy, "'%s' must have at least one lazy field");

    private static boolean areFieldsFinalOrLazy(TypeElement type) {
        boolean lazy = type.getAnnotation(Immutable.class).lazy();
        return getNonStaticFields(type).allMatch(field -> Check.isFinal(field) || (lazy && Check.isVolatile(field)));
    }

    private static boolean areFieldsPrivate(TypeElement type) {
        return getNonStaticFields(type).allMatch(Check::isPrivate);
    }

    private static boolean hasLazyFieldIfLazy(TypeElement type) {
        boolean lazy = type.getAnnotation(Immutable.class).lazy();
        return !lazy || getNonStaticFields(type).anyMatch(Check::isVolatile);
    }
}
