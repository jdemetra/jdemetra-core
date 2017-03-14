/*
 * Copyright 2017 National Bank of Belgium
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
package internal.workspace.file;

import ec.demetra.workspace.file.spi.FamilyHandler;
import ec.tss.DynamicTsVariable;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.regression.TsVariables;
import static internal.test.TestResources.GENERIC_ROOT;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class GenericHandlersTest {

    @Test
    public void testUtilCal() throws IOException {
        FamilyHandler handler = new GenericHandlers.UtilCal();

        GregorianCalendarManager value = (GregorianCalendarManager) handler.read(GENERIC_ROOT, "Calendars");
        assertThat(value.getNames()).containsExactly("Default", "Belgium");
    }

    @Test
    public void testUtilVar() throws IOException {
        FamilyHandler handler = new GenericHandlers.UtilVar();

        TsVariables value = (TsVariables) handler.read(GENERIC_ROOT, "Vars-1");
        assertThat(value.getNames()).containsExactly("x_1", "x_2", "x_3");
        assertThat(value.variables()).hasOnlyElementsOfType(DynamicTsVariable.class);
    }
}
