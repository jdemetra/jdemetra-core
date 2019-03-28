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
package _util;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import javax.annotation.processing.Processor;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.assertj.core.error.BasicErrorMessageFactory;
import org.assertj.core.internal.Failures;
import org.joor.CompileOptions;
import org.joor.Reflect;
import org.joor.ReflectException;

/**
 *
 * @author Philippe Charles
 */
public final class ProcessorAssert extends AbstractAssert<ProcessorAssert, Class<? extends Processor>> {

    public static ProcessorAssert assertThat(Class<? extends Processor> processor) {
        return new ProcessorAssert(processor);
    }

    public ProcessorAssert(Class<? extends Processor> processor) {
        super(processor, ProcessorAssert.class);
    }

    public ProcessorAssert succeedsOn(TypeSpec code) {
        return succeedsOn("", code);
    }

    public ProcessorAssert succeedsOn(String packageName, TypeSpec code) {
        isNotNull();
        try {
            compile(packageName, code, newProcessor());
        } catch (ReflectException ex) {
            failWithMessage(ex.getMessage());
        }
        return this;
    }

    public ThrowableAssertAlternative<ReflectException> failsOn(TypeSpec code) {
        return failsOn("", code);
    }

    public ThrowableAssertAlternative<ReflectException> failsOn(String packageName, TypeSpec code) {
        isNotNull();
        try {
            compile(packageName, code, newProcessor());
            throw Failures.instance().failure(info, new BasicErrorMessageFactory("%nExpecting code to raise a throwable."));
        } catch (ReflectException ex) {
            return new ThrowableAssertAlternative<>(ex);
        }
    }

    public static void compile(String packageName, TypeSpec code, Processor processor) {
        String name = (packageName.isEmpty() ? "" : packageName + ".") + code.name;
        String content = JavaFile.builder(packageName, code).build().toString();
        Reflect.compile(name, content, new CompileOptions().processors(processor));
    }

    private Processor newProcessor() {
        try {
            return actual.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
