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

import ec.satoolkit.diagnostics.FriedmanTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.satoolkit.diagnostics.QSTest;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.eco.Ols;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.modelling.DifferencingResults;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.stats.StatisticalTest;
import ec.tstoolkit.timeseries.regression.RegressionUtilities;
import ec.tstoolkit.timeseries.regression.SeasonalDummies;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SeasonalityTests {

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

    public ec.tstoolkit.information.StatisticalTest qstest(TsData s, int ny, int diff, boolean mean) {
        int freq = s.getFrequency().intValue();
        if (ny != 0) {
            s = s.drop(Math.max(0, s.getLength() - freq * ny), 0);
        }
        DifferencingResults di = DifferencingResults.create(s, diff, mean);
        return ec.tstoolkit.information.StatisticalTest.create(QSTest.compute(di.getDifferenced().internalStorage(), freq, 2));
    }

    public ec.tstoolkit.information.StatisticalTest kruskalWallisTest(TsData s) {
        KruskalWallisTest test=new KruskalWallisTest(s);
        return ec.tstoolkit.information.StatisticalTest.create(test);
    }

    public ec.tstoolkit.information.StatisticalTest friedmanTest(TsData s) {
        FriedmanTest test=new FriedmanTest(s);
        return ec.tstoolkit.information.StatisticalTest.create(test);
    }

    private static ec.tstoolkit.stats.StatisticalTest processAr(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            reg.setY(y.drop(1, 0));
            TsDomain edomain = s.getDomain().drop(1, 0);
            SeasonalDummies dummies = new SeasonalDummies(edomain.getFrequency());
            List<DataBlock> regs = RegressionUtilities.data(dummies, edomain);
            reg.addX(y.drop(0, 1));
            for (DataBlock r : regs) {
                reg.addX(r);
            }
            reg.setMeanCorrection(true);
            int nseas = dummies.getDim();
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            Ols ols = new Ols();
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 2, nseas, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }

    private static StatisticalTest process(TsData s) {
        try {
            RegModel reg = new RegModel();
            DataBlock y = new DataBlock(s);
            y.add(-y.average());
            reg.setY(y);
            TsDomain edomain = s.getDomain();
            SeasonalDummies dummies = new SeasonalDummies(edomain.getFrequency());
            List<DataBlock> regs = RegressionUtilities.data(dummies, edomain);
            for (DataBlock r : regs) {
                reg.addX(r);
            }
            int nseas = dummies.getDim();
            //       BackFilter ur = context.description.getArimaComponent().getDifferencingFilter();
            Ols ols = new Ols();
            if (!ols.process(reg)) {
                return null;
            }
            JointRegressionTest test = new JointRegressionTest(.01);
            test.accept(ols.getLikelihood(), 0, 0, nseas, null);
            return test.getTest();
        } catch (Exception err) {
            return null;
        }
    }
}
