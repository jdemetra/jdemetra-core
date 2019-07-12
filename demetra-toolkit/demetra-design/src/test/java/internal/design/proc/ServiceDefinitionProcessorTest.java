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

import internal.design.proc.ServiceDefinitionProcessor;
import static _util.ProcessorAssert.assertThat;
import com.squareup.javapoet.TypeSpec;
import demetra.design.ServiceDefinition;
import javax.lang.model.element.Modifier;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ServiceDefinitionProcessorTest {

    @Test
    public void testIsInterface() {
        assertThat(ServiceDefinitionProcessor.class)
                .succeedsOn(TypeSpec
                        .interfaceBuilder("testIsInterface1")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(ServiceDefinition.class)
                        .build());

        assertThat(ServiceDefinitionProcessor.class)
                .failsOn(TypeSpec
                        .classBuilder("testIsInterface2")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(ServiceDefinition.class)
                        .build())
                .withMessageContaining("must be an interface");
    }

    @Test
    public void testIsPublic() {
        assertThat(ServiceDefinitionProcessor.class)
                .failsOn(TypeSpec
                        .interfaceBuilder("testIsPublic")
                        .addAnnotation(ServiceDefinition.class)
                        .build())
                .withMessageContaining("must be public");
    }
}
