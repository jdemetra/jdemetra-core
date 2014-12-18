/*
* Copyright 2013 National Bank of Belgium
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

package ec.tstoolkit.information;

import data.Data;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author pcuser
 */
public class InformationSetHelperTest {

    public InformationSetHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testOnX13() {
        TsData s = Data.X;
        CompositeResults process = X13ProcessingFactory.process(s, X13Specification.RSA5);
        InformationSet sel = InformationSetHelper.fromProcResults(process, TsData.class);
        List<Information<TsData>> tssel = sel.deepSelect("*", TsData.class);
        for (Information<TsData> cur : tssel) {
            System.out.println(cur.name);
        }
    }
}
