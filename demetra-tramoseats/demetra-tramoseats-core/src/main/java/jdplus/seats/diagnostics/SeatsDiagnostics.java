/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved 
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
package jdplus.seats.diagnostics;

import demetra.modelling.ComponentInformation;
import demetra.processing.Diagnostics;
import demetra.processing.ProcQuality;
import demetra.sa.ComponentType;
import demetra.sa.SeriesDecomposition;
import demetra.timeseries.TsData;
import java.util.Collections;
import java.util.List;
import jdplus.seats.SeatsResults;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.WienerKolmogorovDiagnostics;

/**
 *
 * @author Kristof Bayens
 */
public class SeatsDiagnostics implements Diagnostics {

    private WienerKolmogorovDiagnostics diags;
//    private ComponentDescriptor[] descs_;
//    private DataBlock[] data_;
    private double bad = SeatsDiagnosticsConfiguration.BAD, uncertain = SeatsDiagnosticsConfiguration.UNC;
    private boolean same, cutoff;

    public static SeatsDiagnostics of(SeatsDiagnosticsConfiguration config, SeatsResults srslts) {
        try {
            if (srslts == null) {
                return null;
            } else {
                SeatsDiagnostics sd=new SeatsDiagnostics(config, srslts);
                if (sd.isValid())
                    return sd;
                else
                    return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public SeatsDiagnostics(SeatsDiagnosticsConfiguration config, SeatsResults rslts) {
        bad = config.getBadThreshold();
        uncertain = config.getUncertainThreshold();
        test(rslts);
    }

    private void test(SeatsResults srslts) {
        same = !srslts.isModelChanged();
        cutoff=srslts.isParametersCutOff();
        SeriesDecomposition decomposition = srslts.getInitialComponents();
        UcarimaModel ucm = srslts.getCompactUcarimaModel();

        int[] cmps = new int[]{1, -2, 2, 3};
        double err = Math.sqrt(srslts.getInnovationVariance());
        TsData t = decomposition.getSeries(ComponentType.Trend, ComponentInformation.Value);
        TsData s = decomposition.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        TsData i = decomposition.getSeries(ComponentType.Irregular, ComponentInformation.Value);
        TsData sa = decomposition.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);

        double[][] data = new double[][]{
            t == null ? null : t.getValues().toArray(),
            sa == null ? null : sa.getValues().toArray(),
            s == null ? null : s.getValues().toArray(),
            i == null ? null : i.getValues().toArray()
        };

        diags = WienerKolmogorovDiagnostics.make(ucm, err, data, cmps);
    }

    public WienerKolmogorovDiagnostics getDiagnostics() {
        return diags;
    }

    public double getBadThreshold() {
        return bad;
    }

    public double getGoodThreshold() {
        return uncertain;
    }
    
    public boolean isValid(){
        return diags != null;
    }

    @Override
    public String getName() {
        return SeatsDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return SeatsDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        if (diags == null) {
            return ProcQuality.Undefined;
        }
        double pval = getValue(test);
        if (Double.isNaN(pval)) {
            return ProcQuality.Undefined;
        }
        if (pval <= bad) {
            return ProcQuality.Bad;
        } else if (pval > uncertain) {
            return ProcQuality.Good;
        } else {
            return ProcQuality.Uncertain;
        }
    }
    private static final int I_CMP = 3, S_CMP = 2;

    @Override
    public double getValue(String test) {
        double val = Double.NaN;
        int icmp = -1, jcmp = -1;
        if (test.equals(SeatsDiagnosticsFactory.SEAS_VAR) || test.equals(SeatsDiagnosticsFactory.SEAS_I_CORR)) {
            icmp = S_CMP;
        }
        if (test.equals(SeatsDiagnosticsFactory.IRR_VAR) || test.equals(SeatsDiagnosticsFactory.SEAS_I_CORR)) {
            jcmp = I_CMP;
        }
        if (test.equals(SeatsDiagnosticsFactory.SEAS_VAR)) {
            val = diags.getPValue(icmp);
        } else if (test.equals(SeatsDiagnosticsFactory.IRR_VAR)) {
            val = diags.getPValue(jcmp);
        } else if (icmp >= 0 && jcmp >= 0) {
            val = diags.getPValue(icmp, jcmp);
        }
        return val;
    }

    @Override
    public List<String> getWarnings() {
        if (!same) {
            return Collections.singletonList(SeatsDiagnosticsFactory.NOTSAME);
        } else if (cutoff){
             return Collections.singletonList(SeatsDiagnosticsFactory.CUTOFF);
        }else{
           return Collections.emptyList();
        }
    }
}
