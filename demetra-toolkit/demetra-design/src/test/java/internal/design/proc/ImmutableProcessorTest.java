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

import internal.design.proc.ImmutableProcessor;
import static _util.ProcessorAssert.assertThat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import demetra.design.Immutable;
import javax.lang.model.element.Modifier;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ImmutableProcessorTest {

    @Test
    public void testIsFinal() {
        assertThat(ImmutableProcessor.class)
                .succeedsOn(TypeSpec
                        .classBuilder("testIsFinal1")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addAnnotation(Immutable.class)
                        .build())
                .failsOn(TypeSpec
                        .classBuilder("testIsFinal2")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Immutable.class)
                        .build())
                .withMessageContaining("must be final");
    }

    @Test
    public void testAreFieldsFinalOrLazy() {
        assertThat(ImmutableProcessor.class)
                .succeedsOn(TypeSpec
                        .classBuilder("testAreFieldsFinalOrLazy1")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(FieldSpec
                                .builder(TypeName.OBJECT, "field", Modifier.PRIVATE, Modifier.FINAL)
                                .initializer("123")
                                .build())
                        .addAnnotation(Immutable.class)
                        .build())
                .failsOn(TypeSpec
                        .classBuilder("testAreFieldsFinalOrLazy2")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(FieldSpec
                                .builder(TypeName.OBJECT, "field", Modifier.PRIVATE)
                                .initializer("123")
                                .build())
                        .addAnnotation(Immutable.class)
                        .build())
                .withMessageContaining("must be final or lazy");

        assertThat(ImmutableProcessor.class)
                .succeedsOn(TypeSpec
                        .classBuilder("testAreFieldsFinalOrLazy3")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(TypeName.OBJECT, "field", Modifier.PRIVATE, Modifier.VOLATILE)
                        .addAnnotation(AnnotationSpec.builder(Immutable.class).addMember("lazy", "true").build())
                        .build())
                .failsOn(TypeSpec
                        .classBuilder("testAreFieldsFinalOrLazy4")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(TypeName.OBJECT, "field", Modifier.PRIVATE)
                        .addAnnotation(AnnotationSpec.builder(Immutable.class).addMember("lazy", "true").build())
                        .build())
                .withMessageContaining("must be final or lazy");
    }

    @Test
    public void testAreFieldsPrivate() {
        assertThat(ImmutableProcessor.class)
                .succeedsOn(TypeSpec
                        .classBuilder("testAreFieldsPrivate1")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(FieldSpec
                                .builder(TypeName.OBJECT, "field", Modifier.FINAL, Modifier.PRIVATE)
                                .initializer("123")
                                .build())
                        .addAnnotation(Immutable.class)
                        .build())
                .failsOn(TypeSpec
                        .classBuilder("testAreFieldsPrivate2")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(FieldSpec
                                .builder(TypeName.OBJECT, "field", Modifier.FINAL)
                                .initializer("123")
                                .build())
                        .addAnnotation(Immutable.class)
                        .build())
                .withMessageContaining("must be private");
    }

    @Test
    public void testHasLazyFieldsIfLazy() {
        assertThat(ImmutableProcessor.class)
                .succeedsOn(TypeSpec
                        .classBuilder("testHasLazyFieldsIfLazy1")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(FieldSpec
                                .builder(TypeName.OBJECT, "field", Modifier.PRIVATE, Modifier.VOLATILE)
                                .initializer("123")
                                .build())
                        .addAnnotation(AnnotationSpec.builder(Immutable.class).addMember("lazy", "true").build())
                        .build())
                .failsOn(TypeSpec
                        .classBuilder("testHasLazyFieldsIfLazy2")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(FieldSpec
                                .builder(TypeName.OBJECT, "field", Modifier.FINAL, Modifier.PRIVATE)
                                .initializer("123")
                                .build())
                        .addAnnotation(AnnotationSpec.builder(Immutable.class).addMember("lazy", "true").build())
                        .build())
                .withMessageContaining("must have at least one lazy field");
    }
}
