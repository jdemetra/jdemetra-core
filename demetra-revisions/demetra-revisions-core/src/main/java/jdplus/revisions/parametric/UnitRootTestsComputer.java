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
import jdplus.stats.tests.DickeyFuller;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class UnitRootTestsComputer {

    public UnitRoot of(DoubleSeq y, int adfk) {

        // dy(t)=a*y(t-1)+e
        DickeyFuller df = DickeyFuller.builder()
                .data(y)
                .type(DickeyFuller.DickeyFullerType.NC)
                .numberOfLags(0)
                .build();
        // dy(t)=c+d*t+a*y(t-1)+e
        DickeyFuller dfc = DickeyFuller.builder()
                .data(y)
                .type(DickeyFuller.DickeyFullerType.CT)
                .numberOfLags(0)
                .build();

        // dy(t)=a*y(t-1)+b1*dy(t-1)+bk*dy(t-k)+e
        DickeyFuller adf = DickeyFuller.builder()
                .data(y)
                .type(DickeyFuller.DickeyFullerType.NC)
                .numberOfLags(adfk)
                .build();
        DickeyFuller pp = DickeyFuller.builder()
                .data(y)
                .type(DickeyFuller.DickeyFullerType.NC)
                .numberOfLags(0)
                .phillipsPerron(true)
                .build();
        return UnitRoot.builder()
                .dickeyFuller(new UnitRoot.Test(df.getRho(), df.getSer(), df.getTest(), df.getPvalue()))
                .dickeyFullerWithTrendAndIntercept(new UnitRoot.Test(dfc.getRho(), dfc.getSer(), dfc.getTest(), dfc.getPvalue()))
                .augmentedDickeyFuller(new UnitRoot.Test(adf.getRho(), adf.getSer(), adf.getTest(), adf.getPvalue()))
                .philipsPerron(new UnitRoot.Test(pp.getRho(), pp.getSer(), pp.getTest(), pp.getPvalue()))
                .build();

    }
}
