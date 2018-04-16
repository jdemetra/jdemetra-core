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
package ec.tstoolkit.jdr.tests;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TradingDaysTests {


    public ec.tstoolkit.information.StatisticalTest ftest(TsData s, boolean ar, int ny) {
        int ifreq = s.getFrequency().intValue();

        if (ar) {
            if (ny != 0) {
                s = s.drop(Math.max(0, s.getLength() - ifreq * ny - 1), 0);
            }
            return ec.tstoolkit.information.StatisticalTest.create(processAr(s));
        } else {
            s = s.delta(1);
            if (ny != 0) {
                s = s.drop(Math.max(0, s.getLength() - ifreq * ny), 0);
            }
            return ec.tstoolkit.information.StatisticalTest.create(process(s));
        }
    }

    private static StatisticalTest process(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            y.add(-y.average());
            reg.setY(y);
            GregorianCalendarVariables tdvars = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
            int ntd = tdvars.getDim();
            TsDomain edomain = s.getDomain();
            List<DataBlock> bvars = new ArrayList<>(ntd);
            for (int i = 0; i < ntd; ++i) {
                bvars.add(new DataBlock(edomain.getLength()));
            }
            tdvars.data(edomain, bvars);
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            for (int i = 0; i < ntd; ++i) {
                DataBlock cur = bvars.get(i);
                reg.addX(cur);
            }
 //           reg.setMeanCorrection(true);
            Ols ols = new Ols();
            
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 0, ntd, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }

    private static StatisticalTest processAr(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            reg.setY(y.drop(1, 0));
            GregorianCalendarVariables tdvars = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
            int ntd = tdvars.getDim();
            TsDomain edomain = s.getDomain().drop(1, 0);
            List<DataBlock> bvars = new ArrayList<>(ntd);
            for (int i = 0; i < ntd; ++i) {
                bvars.add(new DataBlock(edomain.getLength()));
            }
            tdvars.data(edomain, bvars);
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            reg.addX(y.drop(0, 1));
            for (int i = 0; i < ntd; ++i) {
                DataBlock cur = bvars.get(i);
                reg.addX(cur);
            }
            Ols ols = new Ols();
            reg.setMeanCorrection(true);
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 2, ntd, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }

}
