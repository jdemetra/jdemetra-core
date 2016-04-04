/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
/*
 */
package ec.businesscycle.simplets;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class TsHodrickPrescottTest {
    
    public TsHodrickPrescottTest() {
    }

    @Ignore
    @Test
    public void testDefaultMethod() {
        CompositeResults ts = TramoSeatsProcessingFactory.process(Data.P, TramoSeatsSpecification.RSAfull);
        TsHodrickPrescott hp=new TsHodrickPrescott();
        hp.process(ts.getData("t", TsData.class));
        TsDataTable table=new TsDataTable();
        table.add(hp.getCycle());
        table.add(hp.getTrend());
        System.out.println(table);
    }
    
}
