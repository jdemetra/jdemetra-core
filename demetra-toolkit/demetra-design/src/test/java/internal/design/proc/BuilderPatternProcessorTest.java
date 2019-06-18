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
package internal.design.proc;

import internal.design.proc.BuilderPatternProcessor;
import static _util.ProcessorAssert.assertThat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import demetra.design.BuilderPattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class BuilderPatternProcessorTest {

    @Test
    public void testHasBuildMethod() {
        assertThat(BuilderPatternProcessor.class)
                .succeedsOn(TypeSpec
                        .classBuilder("testHasBuildMethod1")
                        .addMethod(MethodSpec
                                .methodBuilder("build")
                                .returns(Byte.class)
                                .addStatement("return 0")
                                .build())
                        .addAnnotation(byteBuilder)
                        .build());

        assertThat(BuilderPatternProcessor.class)
                .failsOn(TypeSpec
                        .classBuilder("testHasBuildMethod2")
                        .addMethod(MethodSpec
                                .methodBuilder("buildx")
                                .returns(Byte.class)
                                .addStatement("return 0")
                                .build())
                        .addAnnotation(byteBuilder)
                        .build())
                .withMessageContaining("Cannot find build method");

        assertThat(BuilderPatternProcessor.class)
                .failsOn(TypeSpec
                        .classBuilder("testHasBuildMethod3")
                        .addMethod(MethodSpec
                                .methodBuilder("build")
                                .returns(String.class)
                                .addStatement("return null")
                                .build())
                        .addAnnotation(byteBuilder)
                        .build())
                .withMessageContaining("Cannot find build method");
    }

    @Test
    public void testBuildMethodWithTypeAnnotation() {
        assertThat(BuilderPatternProcessor.class)
                .succeedsOn(TypeSpec
                        .classBuilder("testBuildMethodWithTypeAnnotation")
                        .addMethod(MethodSpec
                                .methodBuilder("build")
                                .returns(Byte.class)
                                .addStatement("return 0")
                                .addAnnotation(NonNull.class)
                                .build())
                        .addAnnotation(byteBuilder)
                        .build());
    }

    private final AnnotationSpec byteBuilder = AnnotationSpec
            .builder(BuilderPattern.class)
            .addMember("value", "$T.class", Byte.class)
            .build();
}
