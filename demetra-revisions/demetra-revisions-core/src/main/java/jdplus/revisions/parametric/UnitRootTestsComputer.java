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
import demetra.revisions.parametric.UnitRoot;
import demetra.stats.ProbabilityType;
import jdplus.dstats.T;
import jdplus.stats.tests.AugmentedDickeyFuller;
import jdplus.stats.tests.DickeyFullerTable;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class UnitRootTestsComputer {
    
    public UnitRoot of(DoubleSeq y, int adfk) {
        
        // dy(t)=a*y(t-1)+e
        AugmentedDickeyFuller df = AugmentedDickeyFuller.builder()
                .data(y)
                .constant(false)
                .linearTrend(false)
                .numberOfLags(0)
                .build();
        // dy(t)=c+d*t+a*y(t-1)+e
        AugmentedDickeyFuller dfc = AugmentedDickeyFuller.builder()
                .data(y)
                .constant(true)
                .linearTrend(true)
                .numberOfLags(0)
                .build();
        
        // dy(t)=a*y(t-1)+b1*dy(t-1)+bk*dy(t-k)+e
        AugmentedDickeyFuller adf = AugmentedDickeyFuller.builder()
                .data(y)
                .constant(false)
                .linearTrend(false)
                .numberOfLags(adfk)
                .build();
        T t=new T(y.length()-2);
        return UnitRoot.builder()
                .dickeyFuller(new UnitRoot.Test(df.getRho(),df.getStdErr(),df.getT(),DickeyFullerTable.probability(y.length(), df.getT(), DickeyFullerTable.DickeyFullerType.NC)))
                .dickeyFullerWithTrendAndIntercept(new UnitRoot.Test(dfc.getRho(),dfc.getStdErr(),dfc.getT(),DickeyFullerTable.probability(y.length(), dfc.getT(), DickeyFullerTable.DickeyFullerType.CT)))
                .augmentedDickeyFuller(new UnitRoot.Test(adf.getRho(),adf.getStdErr(),adf.getT(),DickeyFullerTable.probability(y.length(), adf.getT(), DickeyFullerTable.DickeyFullerType.NC)))
                .philipsPerron(new UnitRoot.Test(df.getRho()-1,df.getStdErr(),df.getT(),
                        t.getProbability((df.getRho()-1)/df.getStdErr(), ProbabilityType.Lower)))
                .build();
        
    }
 }
