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
import ec.tss.TsMoniker;
import ec.tss.sa.SaProcessing;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import static internal.test.TestResources.GENERIC_ROOT;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class GenericHandlersTest {

    private static final String C0S0_LABEL = "S1 - 000854628";
    private static final String C0S0_URI = "demetra://tsprovider/Xml/20111201/SERIES?file=Insee.xml#collectionIndex=0&seriesIndex=0";
    private static final TsDomain C0S0_DOMAIN = new TsDomain(TsFrequency.Monthly, 1990, 0, 224);

    private static final String C0S1_URI = "demetra://tsprovider/Xml/20111201/SERIES?file=Insee.xml#collectionIndex=0&seriesIndex=1";

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

        assertThat(value.variables()).hasOnlyElementsOfType(DynamicTsVariable.class).hasSize(3);

        assertThat((DynamicTsVariable) value.get("x_1")).satisfies(o -> {
            assertThat(o.getName()).isEqualTo("x_1");
            assertThat(o.getDescription()).isEqualTo(C0S0_LABEL);
            assertThat(o.getTsData().getDomain()).isEqualTo(C0S0_DOMAIN);
            assertThat(o.getMoniker()).isEqualTo(new TsMoniker("Xml", C0S0_URI));
        });

        assertThat((DynamicTsVariable) value.get("x_2")).satisfies(o -> {
            assertThat(o.getName()).isEqualTo("x_2");
            assertThat(o.getDescription()).isEqualTo("x_2");
            assertThat(o.getTsData()).isNull();
            assertThat(o.getMoniker()).isEqualTo(new TsMoniker("Xml", C0S1_URI));
        });
    }

    @Test
    public void testSaMulti() throws IOException {
        FamilyHandler handler = new GenericHandlers.SaMulti();

        SaProcessing value = (SaProcessing) handler.read(GENERIC_ROOT, "SAProcessing-1.xml");
        assertThat(value.size()).isEqualTo(15);

        assertThat(value.get(0)).satisfies(o -> {
            assertThat(o.getTs().getMetaData())
                    .containsEntry("@timestamp", "Tue Feb 21 13:57:38 CET 2017")
                    .containsEntry("@source", "Xml")
                    .containsEntry("@id", C0S0_URI)
                    .hasSize(3);
            assertThat(o.getTs().getTsData().getDomain()).isEqualTo(C0S0_DOMAIN);
        });

        assertThat(value.get(1)).satisfies(o -> {
            assertThat(o.getTs().getMetaData())
                    .containsEntry("@timestamp", "Tue Feb 21 13:57:38 CET 2017")
                    .containsEntry("@source", "Xml")
                    .containsEntry("@id", C0S1_URI)
                    .hasSize(3);
            assertThat(o.getTs().getTsData()).isNull();
        });
    }
}
