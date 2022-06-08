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
package jdplus.regarima.ami;

import demetra.data.Data;
import demetra.timeseries.TsData;

/**
 *
 * @author PALATEJ
 */
public class GenericLogLevelModuleTest {

    public GenericLogLevelModuleTest() {
    }

    public static void testInsee() {
        TsData[] all = Data.insee();
        for (int i = 0; i < all.length; ++i) {
            GenericLogLevelModule ll = GenericLogLevelModule.builder().build();
            ll.process(all[i].getValues());
            System.out.print(ll.getLog());
            System.out.print('\t');
            System.out.println(ll.getLevel());
        }
    }

    public static void testInseeRecursive() {
        TsData[] all = Data.insee();
        for (int i = 0; i < all.length; ++i) {
            GenericLogLevelModule ll = new GenericLogLevelModule.Builder().build();
            for (int j = 0; j < 36; ++j) {
                ll.process(all[i].drop(0, j).getValues());
                System.out.print(ll.isChoosingLog() ? 1 : 0);
                System.out.print('\t');
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args){
        testInsee();
        testInseeRecursive();
    }
}
