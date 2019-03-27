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
package demetra.design;

import static _util.ProcessorAssert.assertThat;
import com.squareup.javapoet.TypeSpec;
import javax.lang.model.element.Modifier;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class MightBePromotedProcessorTest {

    @Test
    public void testInternalOrNotPublic() {
        assertThat(MightBePromotedProcessor.class)
                .succeedsOn("internal.abc", TypeSpec
                        .classBuilder("testInternalOrNotPublic1")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(MightBePromoted.class)
                        .build())
                .succeedsOn("abc.internal", TypeSpec
                        .classBuilder("testInternalOrNotPublic2")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(MightBePromoted.class)
                        .build())
                .succeedsOn("impl.abc", TypeSpec
                        .classBuilder("testInternalOrNotPublic3")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(MightBePromoted.class)
                        .build())
                .succeedsOn("abc.impl", TypeSpec
                        .classBuilder("testInternalOrNotPublic4")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(MightBePromoted.class)
                        .build());

        assertThat(MightBePromotedProcessor.class)
                .succeedsOn("abc", TypeSpec
                        .classBuilder("testInternalOrNotPublic5")
                        .addAnnotation(MightBePromoted.class)
                        .build())
                .failsOn("abc", TypeSpec
                        .classBuilder("testInternalOrNotPublic6")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(MightBePromoted.class)
                        .build());
    }
}
