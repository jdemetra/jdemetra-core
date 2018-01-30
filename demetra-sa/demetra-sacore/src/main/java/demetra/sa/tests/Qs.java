/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.sa.tests;

import demetra.data.DoubleSequence;
import demetra.design.IBuilder;
import demetra.stats.tests.LjungBox;
import demetra.stats.tests.StatisticalTest;

/**
 *
 * @author Jean Palate
 */
public class Qs  implements IBuilder<StatisticalTest>{

    private final LjungBox lb;
    
    public Qs(DoubleSequence sample, int seasLag){
        lb=new LjungBox(sample)
                .lag(seasLag)
                .autoCorrelationsCount(3)
                .usePositiveAutoCorrelations();
     }

    /**
     *
     * @param nhp
     * @return
     */
    public Qs hyperParametersCount(int nhp) {
        lb.hyperParametersCount(nhp);
        return this;
    }


     /**
     *
     * @param k
     * @return
     */
    public Qs autoCorrelationsCount(int k) {
        lb.autoCorrelationsCount(k);
        return this;
    }
    
    public Qs useNegativeAutocorrelations(){
        lb.useNegativeAutoCorrelations();
        return this;
    }

    public Qs usePositiveAutocorrelations(){
        lb.usePositiveAutoCorrelations();
        return this;
    }

    public Qs useAllAutocorrelations(){
        lb.useAllAutoCorrelations();
        return this;
    }

    @Override
    public StatisticalTest build() {
        return lb.build();            
    }

 }
