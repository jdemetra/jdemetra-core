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

package ec.tstoolkit.timeseries.simplets;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class YearIteratorTest {
    
    public YearIteratorTest() {
    }

    @Test
    public void testFull() {
        TsData s=new TsData(TsFrequency.Monthly, 1980, 3, 97);
        for (int i=0; i<s.getLength(); ++i){
            s.set(i, i+1);
        }
        
        YearIterator p=YearIterator.fullYears(s);
        int[] x=new int[s.getDomain().getFullYearsCount()];
        int i=0;
        while (p.hasMoreElements()){
            x[i++]=(int)p.nextElement().data.sum();
        }
        
        assertEquals(x[0], 186);
        for (int j=1; j<x.length; ++j){
            assertEquals(x[j]-x[j-1], 144);
        }
    }
    
    @Test
    public void test() {
        TsData s=new TsData(TsFrequency.Monthly, 1980, 3, 97);
        for (int i=0; i<s.getLength(); ++i){
            s.set(i, i+1);
        }
        
        YearIterator p=new YearIterator(s);
        int[] x=new int[s.getDomain().getYearsCount()];
        p.reset();
        int i=0;
        while (p.hasMoreElements()){
            x[i++]=(int)p.nextElement().data.sum();
        }
        
        // should , 
        assertEquals(x[0], 45);
        assertEquals(x[1], 186);
        for (int j=2; j<x.length-1; ++j){
            assertEquals(x[j]-x[j-1], 144);
        }
        assertEquals(x[x.length-1], 382);
   }
}
