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

import demetra.data.DoubleSeq;
import jdplus.regarima.outlier.RobustStandardDeviationComputer;

/**
 *
 * @author PALATEJ
 */
public class RobustLogLevelModule {

    /**
     * @return the log
     */
    public double getLog() {
        return log;
    }

    /**
     * @return the level
     */
    public double getLevel() {
        return level;
    }

    private IGenericDifferencingModule differencing = FastDifferencingModule.builder().build();
    private int[] periodicities = new int[]{1, 12};
    private double log, level;

    public void process(DoubleSeq data) {
        DoubleSeq ldata = data.log();
        int[] orders = differencing.process(data, periodicities, null);
        int[] lorders = differencing.process(ldata, periodicities, null);

        for (int i = 0; i < orders.length; ++i) {
            if (lorders[i] > orders[i]) {
                orders[i] = lorders[i];
            }
        }
        // compute minimal differencing
        DoubleSeq d = data;
        DoubleSeq ld = ldata;
        int del = 0;
        for (int i = 0; i < orders.length; ++i) {
            d = d.delta(periodicities[i], orders[i]);
            ld = ld.delta(periodicities[i], orders[i]);
            del += periodicities[i] * orders[i];
        }

        double std = RobustStandardDeviationComputer.mad(95, true).compute(d);
        double lstd = RobustStandardDeviationComputer.mad(95, true).compute(ld);

        // we exclude figures that are really abnormal in one transformation
        double slog = 0, ssq = 0, lssq = 0;
        int n = 0;
        for (int i = 0; i < d.length(); ++i) {
            double cur = d.get(i), lcur = ld.get(i);
            if (Math.abs(cur) < 4 * std || Math.abs(lcur) < 4 * lstd) {
                slog += Math.log(data.get(i + del));
                ssq += cur * cur;
                lssq += lcur * lcur;
                ++n;
            }
        }
        level=Math.log(ssq); 
        log=Math.log(lssq)+2*slog/n;
    }
    
    public boolean isChoosingLog(){
        return getLog()<getLevel();
    }
}
