/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.stl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Jean Palate
 */
public class IPeriodicDataOrganizerTest {
    
    public IPeriodicDataOrganizerTest() {
    }

    @Test
    public void testFull() {
        IPeriodicDataOrganizer o = IPeriodicDataOrganizer.of(12);
        double[] x=new double[36];
        IPeriodicDataSelectors z = o.selectors(IDataSelector.of(x,-12));
        int n=0;
        for (int i=0; i<o.getPeriod(); ++i){
            IDataSelector s = z.get(i);
            n+=s.getLength();
        }
        assertEquals(x.length, n);
    }
    
    @Test
    public void testPartial() {
        IPeriodicDataOrganizer o = IPeriodicDataOrganizer.of(12);
        double[] x=new double[40];
        IPeriodicDataSelectors z = o.selectors(IDataSelector.of(x,-12));
        int n=0;
        for (int i=0; i<o.getPeriod(); ++i){
            IDataSelector s = z.get(i);
            n+=s.getLength();
        }
        assertEquals(x.length, n);
    }
}
