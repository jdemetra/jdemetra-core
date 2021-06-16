/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.x13.io.information;

import demetra.data.Data;
import demetra.information.InformationSet;
import demetra.sa.SaDefinition;
import demetra.sa.SaItem;
import demetra.sa.SaItems;
import demetra.sa.SaSpecification;
import demetra.sa.io.information.SaItemMapping;
import demetra.sa.io.information.SaItemsMapping;
import demetra.timeseries.Ts;
import demetra.timeseries.TsMoniker;
import demetra.util.NameManager;
import demetra.x13.X13Spec;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class X13SpecMappingTest {

    public X13SpecMappingTest() {
    }

    @Test
    public void testSaItem() {
        Ts ts = Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();

        SaDefinition sadef = SaDefinition.builder()
                .domainSpec(X13Spec.RSA5)
                .ts(ts)
                .build();

        SaItem item = SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(false);
        NameManager<SaSpecification> mgr = SaItemsMapping.defaultNameManager();
        InformationSet info = SaItemMapping.write(item, mgr, true);
        
        SaItem nitem = SaItemMapping.read(info, mgr, Collections.emptyMap());
        nitem.process(true);
    }

    @Test
    public void testSaItems() {
        Ts ts = Ts.builder()
                .moniker(TsMoniker.of())
                .name("prod")
                .data(Data.TS_PROD)
                .build();

        SaDefinition sadef = SaDefinition.builder()
                .domainSpec(X13Spec.RSA5)
                .ts(ts)
                .build();

        SaItem item = SaItem.builder()
                .name("prod")
                .definition(sadef)
                .build();
        item.process(false);
        
        SaItems items = SaItems.builder()
                .item(item)
                .build();
        
        InformationSet info = SaItemsMapping.write(items, true);
        
        SaItems nitems = SaItemsMapping.read(info);
        nitems.getItems().forEach(v->v.process(true));
    }
}
