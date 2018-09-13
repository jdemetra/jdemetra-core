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
package ec.satoolkit.x11;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class TrendCycleFilterFactoryTest {

    public TrendCycleFilterFactoryTest() {
    }

    /**
     * Test of makeHendersonFilter method, of class TrendCycleFilterFactory.
     */
    @Test
    public void testMakeHendersonFilter() {
        for (int i = 1; i < 100; i += 2) {
            SymmetricFilter filter = TrendCycleFilterFactory.makeHendersonFilter(i);
            DataBlock w = new DataBlock(filter.getWeights());
            assertTrue(Math.abs(w.sum() - 1) < 1e-9);
        }
    }

    /**
     * Test of makeHendersonFilter method, of class TrendCycleFilterFactory.
     */
    //  @Test
    //  public void getHendersonFilterWeights() {
    //      ArrayList<Integer> freqListe = new ArrayList<>();
    //      freqListe.add(4);
    //      freqListe.add(12);
    //       ArrayList<Integer> hendersonListe = new ArrayList<>();
    //       hendersonListe.add(5);
    //       hendersonListe.add(9);
    //       hendersonListe.add(13);
    //       hendersonListe.add(23);
    //       for (int freq : freqListe) {
    //           System.out.println("Frequency: " + freq);
//            for (int length : hendersonListe) {
//                SymmetricFilter filter = TrendCycleFilterFactory.makeHendersonFilter(length);
//                DataBlock w = new DataBlock(filter.getWeights());
    //               System.out.println("Henderson length:" + length);
    //               for (int j = 0; j < w.getEndIndex(); j++) {
    //                   System.out.println(j + ": " + w.getData()[j]);
    //               }
    //IFiniteFilter[] mustgraveFiltersForHenderson = MusgraveFilterFactory.makeFiltersForHenderson(length, freq, 0.0);
    //               IFiniteFilter[] mustgraveFiltersForHenderson = MusgraveFilterFactory.makeFiltersForHenderson(length, freq);
//                for (int k = 0; k < mustgraveFiltersForHenderson.length; k++) {
//                    IFiniteFilter musgraveFilter = mustgraveFiltersForHenderson[k];
//                    DataBlock v = new DataBlock(musgraveFilter.getWeights());
//                    System.out.println("Musgrave:-" + k);
//                    for (int j = 0; j < v.getEndIndex(); j++) {
//                        System.out.println(j + ": " + v.getData()[j]);
//                    }
//                }
//            }
//        }
    //   }
    /**
     * Test of makeTrendFilter method, of class TrendCycleFilterFactory.
     */
    @Test
    public void testMakeTrendFilter() {
        for (int i = 1; i < 100; ++i) {
            SymmetricFilter filter = TrendCycleFilterFactory.makeTrendFilter(i);
            DataBlock w = new DataBlock(filter.getWeights());
            assertTrue(Math.abs(w.sum() - 1) < 1e-9);
        }
    }
}
