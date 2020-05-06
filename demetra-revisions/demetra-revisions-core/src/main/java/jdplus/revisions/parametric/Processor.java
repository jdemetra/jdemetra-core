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
package jdplus.revisions.parametric;

import demetra.data.DoubleSeq;
import demetra.revisions.parametric.Bias;
import demetra.revisions.parametric.RegressionBasedAnalysis;
import demetra.revisions.timeseries.TsDataVintages;
import demetra.timeseries.TsData;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Processor {

    public <K extends Comparable> RegressionBasedAnalysis regressionBasedAnalysis(TsDataVintages<K> vintages, int nrevs) {
        RegressionBasedAnalysis.Builder builder=RegressionBasedAnalysis.builder();
        bias(vintages, nrevs, builder);
        return builder.build();
    }
    
    private <K extends Comparable> void bias(TsDataVintages<K> vintages, int nrevs,RegressionBasedAnalysis.Builder builder ){
        TsData preliminary=vintages.preliminary(), current=vintages.current();
        builder.currentBias(BiasComputer.of(TsData.subtract(current, preliminary).getValues()));
        TsData prev=preliminary;
        for (int i=0; i<nrevs; ++i){
            TsData cur=vintages.vintage(i+1);
            if (cur == null)
                break;
            DoubleSeq rev=TsData.subtract(cur, prev).getValues();
            Bias bcur=BiasComputer.of(rev);
            if (bcur == null)
                break;
            builder.revisionBias(bcur);
            prev=cur;
        }
    }
}
