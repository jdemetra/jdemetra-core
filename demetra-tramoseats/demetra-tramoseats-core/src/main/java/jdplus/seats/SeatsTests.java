/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.seats;

import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.SeriesDecomposition;
import demetra.timeseries.TsData;
import jdplus.sarima.SarimaModel;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.WienerKolmogorovDiagnostics;

/**
 *
 * @author palatej
 */
public class SeatsTests {

    private final SeatsResults results;

    private volatile WienerKolmogorovDiagnostics wkDiagnostics;

    public SeatsTests(SeatsResults results) {
        this.results = results;
    }

    public WienerKolmogorovDiagnostics wkDiagnostics() {
        WienerKolmogorovDiagnostics tests = wkDiagnostics;
        if (tests == null) {
            synchronized (this) {
                tests = wkDiagnostics;
                if (tests == null) {
                    SeriesDecomposition decomposition = results.getInitialComponents();
                    UcarimaModel ucm = results.getCompactUcarimaModel();

                    int[] cmps = new int[]{1, -2, 2, 3};
                    double err = Math.sqrt(results.getInnovationVariance());
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

                    tests = WienerKolmogorovDiagnostics.make(ucm, err, data, cmps);
                    wkDiagnostics = tests;
                }
            }
        }
        return tests;
    }

    public boolean isModelChanged() {
        return results.isModelChanged();
    }

    public boolean isParametersCutOff() {
        return results.isParametersCutOff();
    }

    public SarimaModel finalModel() {
        return results.getFinalModel();
    }

    public SarimaModel originalModel() {
        return results.getOriginalModel();
    }

    public UcarimaModel ucarimaModel() {
        return results.getUcarimaModel();
    }

}
