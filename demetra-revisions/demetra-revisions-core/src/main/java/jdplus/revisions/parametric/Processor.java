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
import demetra.revisions.parametric.RevisionAnalysis;
import demetra.revisions.timeseries.TsDataVintages;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import java.util.List;
import jdplus.stats.StatUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Processor {
    
    

    public <K extends Comparable> RegressionBasedAnalysis<K> regressionBasedAnalysis(TsDataVintages<K> all, RegressionBasedAnalysis.Type type) {
        
        switch (type){
            case Vertical:
                return vanalysis(all);
            case Diagonal:
                return danalysis(all);
            default:
                return null;
        }
    }

    private static <K extends Object & Comparable> RegressionBasedAnalysis<K> vanalysis(TsDataVintages<K> all) {
        RegressionBasedAnalysis.Builder builder=RegressionBasedAnalysis.builder();
        List<K> vintages = all.getVintages();
        TsData preliminary=all.vintage(vintages.get(0));
        TsData prev=preliminary;
        for (int i=1; i<vintages.size(); ++i){
                    
            K k=vintages.get(i);
            RevisionAnalysis.Builder<K> analysis = RevisionAnalysis.<K>builder()
                    .vintage(k);
            TsData cur=all.vintage(k);
            if (cur == null)
                break;
            
            // common domain
            TsDomain common=cur.getDomain().intersection(preliminary.getDomain());
            if (common.isEmpty())
                break;
            DoubleSeq v0=TsData.fitToDomain(preliminary, common).getValues(),
                    v1=TsData.fitToDomain(cur, common).getValues();
            // first Vi % V
            analysis.theilCoefficient(StatUtility.theilInequalityCoefficient(v1, v0))
                    .regression(OlsTestComputer.of(v1, v0));
            
            // than revisions
            DoubleSeq rev=TsData.subtract(cur, prev).getValues();
            Bias bias=BiasComputer.of(rev);
            if (bias != null)
                analysis.bias(bias);
            prev=cur;
            builder.revision(analysis.build());
        }
        return builder.build();
    }

    private static <K extends Object & Comparable> RegressionBasedAnalysis<K> danalysis(TsDataVintages<K> all) {
        RegressionBasedAnalysis.Builder builder=RegressionBasedAnalysis.builder();
        TsData preliminary=all.preliminary();
        TsData prev=preliminary;
        int nmax=all.maxRevisionsCount();
        for (int i=1; i<nmax; ++i){
                    
            RevisionAnalysis.Builder<K> analysis = RevisionAnalysis.<K>builder();
            TsData cur=all.vintage(i);
            if (cur == null)
                break;
            
            // common domain
            TsDomain common=cur.getDomain().intersection(preliminary.getDomain());
            if (common.isEmpty())
                break;
            DoubleSeq v0=TsData.fitToDomain(preliminary, common).getValues(),
                    v1=TsData.fitToDomain(cur, common).getValues();
            // first Vi % V
            analysis.theilCoefficient(StatUtility.theilInequalityCoefficient(v1, v0))
                    .regression(OlsTestComputer.of(v1, v0));
            
            // than revisions
            DoubleSeq rev=TsData.subtract(cur, prev).getValues();
            Bias bias=BiasComputer.of(rev);
            if (bias != null)
                analysis.bias(bias);
            prev=cur;
            builder.revision(analysis.build());
        }
        return builder.build();
    }
    
}
