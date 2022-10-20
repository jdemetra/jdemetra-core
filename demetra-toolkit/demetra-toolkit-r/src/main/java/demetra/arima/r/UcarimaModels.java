/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.r;

import demetra.data.DoubleSeq;
import demetra.math.matrices.Matrix;
import demetra.modelling.io.protobuf.ModellingProtos;
import demetra.modelling.io.protobuf.ModellingProtosUtility;
import java.util.function.DoubleUnaryOperator;
import jdplus.arima.ArimaModel;
import jdplus.math.matrices.FastMatrix;
import jdplus.sarima.SarimaModel;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.composite.CompositeSsf;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.SsfData;
import jdplus.ucarima.UcarimaModel;
import jdplus.ucarima.WienerKolmogorovEstimator;
import jdplus.ucarima.WienerKolmogorovEstimators;
import jdplus.ucarima.WienerKolmogorovPreliminaryEstimatorProperties;
import jdplus.ssf.arima.SsfUcarima;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class UcarimaModels {

    public UcarimaModel of(ArimaModel model, ArimaModel[] components) {
        return UcarimaModel.builder()
                .model(model)
                .add(components).build();
    }
    
    /**
     * 
     * @param sarima Model to be decomposed
     * @param rmod Trend tolerance
     * @param epsphi Seasonal tolerance in degrees
     * @return 
     */
    public UcarimaModel decompose(SarimaModel sarima, double rmod, double epsphi){
        TrendCycleSelector tsel = new TrendCycleSelector();
        if (rmod > 0)
            tsel.setBound(rmod);
        SeasonalSelector ssel = new SeasonalSelector(sarima.getPeriod());
        if (epsphi > 0)
            ssel.setTolerance(epsphi);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
        
    }

    public UcarimaModel doCanonical(UcarimaModel ucm, int cmp, boolean adjust) {
        return ucm.setVarianceMax(cmp, adjust);
    }

    public double[] wienerKolmogorovFilter(UcarimaModel ucm, int cmp, boolean signal, int nweights) {
        WienerKolmogorovEstimators wks = new WienerKolmogorovEstimators(ucm);
        WienerKolmogorovEstimator wk = wks.finalEstimator(cmp, signal);
        return DoubleSeq.onMapping(nweights, wk.getWienerKolmogorovFilter().weights()).toArray();
    }

    public double[] wienerKolmogorovFilterGain(UcarimaModel ucm, int cmp, boolean signal, int n) {
        WienerKolmogorovEstimators wks = new WienerKolmogorovEstimators(ucm);
        WienerKolmogorovEstimator wk = wks.finalEstimator(cmp, signal);
        DoubleUnaryOperator gain = wk.getWienerKolmogorovFilter().gainFunction();
        double[] g = new double[n];
        double q = Math.PI / (n - 1);
        for (int i = 0; i < n; ++i) {
            double w = q * i;
            g[i] = gain.applyAsDouble(w);
        }
        return g;
    }

    public WienerKolmogorovEstimators wienerKolmogorovEstimators(UcarimaModel ucm) {
        return new WienerKolmogorovEstimators(ucm);
    }

    public WienerKolmogorovEstimator finalEstimator(WienerKolmogorovEstimators wk, int cmp, boolean signal) {
        return wk.finalEstimator(cmp, signal);
    }

    public double[] gain(WienerKolmogorovEstimator wk, int n) {
        DoubleUnaryOperator gain = wk.getWienerKolmogorovFilter().gainFunction();
        double[] g = new double[n];
        double q = Math.PI / (n - 1);
        for (int i = 0; i < n; ++i) {
            double w = q * i;
            g[i] = gain.applyAsDouble(w);
        }
        return g;
    }

    public double[] filter(WienerKolmogorovEstimator wk, int n) {
        return DoubleSeq.onMapping(n + 1, wk.getWienerKolmogorovFilter().weights()).toArray();
    }

    public double[] spectrum(WienerKolmogorovEstimator wk, int n) {
        DoubleUnaryOperator s = wk.getEstimatorModel().getSpectrum().asFunction();
        double[] g = new double[n];
        double q = Math.PI / (n - 1);
        for (int i = 0; i < n; ++i) {
            double w = q * i;
            g[i] = s.applyAsDouble(w);
        }
        return g;
    }

    public WienerKolmogorovPreliminaryEstimatorProperties preliminaryEstimators(WienerKolmogorovEstimators wk, int cmp, boolean signal) {
        WienerKolmogorovPreliminaryEstimatorProperties wkp = new WienerKolmogorovPreliminaryEstimatorProperties(wk);
        wkp.select(cmp, signal);
        return wkp;
    }

    public Matrix estimate(double[] data, UcarimaModel ucm, boolean stdev) {
        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        DefaultSmoothingResults rslt = DkToolkit.sqrtSmooth(ssf, new SsfData(data), stdev, true);
        int n = ucm.getComponentsCount();
        FastMatrix M = FastMatrix.make(data.length, stdev ? 2 * n : n);
        int[] pos = ssf.componentsPosition();
        for (int i = 0; i < n; ++i) {
            M.column(i).copy(rslt.getComponent(pos[i]));
            if (stdev) {
                M.column(n + i).copy(rslt.getComponentVariance(pos[i]).fastOp(w -> w <= 0 ? 0 : Math.sqrt(w)));
            }
        }

        return M;
    }

    public byte[] toBuffer(UcarimaModel model) {
        ModellingProtos.UcarimaModel.Builder builder = ModellingProtos.UcarimaModel.newBuilder()
                .setModel(ModellingProtosUtility.convert(model.getModel(), "model"));

        for (int i = 0, j = 0; i < model.getComponentsCount(); ++i) {
            ArimaModel component = model.getComponent(i);
            if (!component.isNull()) {
                builder.addComponents(ModellingProtosUtility.convert(component, "cmp-" + (++j)));
            }
        }
        return builder.build().toByteArray();
    }
}
