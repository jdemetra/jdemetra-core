/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.r;

import demetra.arima.ArimaModel;
import demetra.arima.UcarimaModel;
import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.modelling.ApiUtility;
import jdplus.ucarima.WienerKolmogorovEstimator;
import jdplus.ucarima.WienerKolmogorovEstimators;
import jdplus.ucarima.WienerKolmogorovPreliminaryEstimatorProperties;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class UcarimaModels {

    public UcarimaModel of(ArimaModel model, ArimaModel[] components) {
        return new UcarimaModel(model, components);
    }

    public UcarimaModel doCanonical(UcarimaModel ucm, int cmp, boolean adjust) {
        jdplus.ucarima.UcarimaModel m = ApiUtility.fromApi(ucm);
        jdplus.ucarima.UcarimaModel nm = m.setVarianceMax(cmp, adjust);
        int ncmps = ucm.size();
        String[] names = new String[cmp < 0 ? ncmps + 1 : ncmps];
        for (int i = 0; i < ncmps; ++i) {
            names[i] = ucm.getComponent(i).getName();
        }
        if (ncmps<names.length){
            names[ncmps]="noise";
        }
        return ApiUtility.toApi(nm, names);
    }

    public double[] wienerKolmogorovFilter(UcarimaModel ucarima, int cmp, boolean signal, int nweights) {
        jdplus.ucarima.UcarimaModel ucm = ApiUtility.fromApi(ucarima);
        WienerKolmogorovEstimators wks = new WienerKolmogorovEstimators(ucm);
        WienerKolmogorovEstimator wk = wks.finalEstimator(cmp, signal);
        return DoubleSeq.onMapping(nweights, wk.getWienerKolmogorovFilter().weights()).toArray();
    }

    public double[] wienerKolmogorovFilterGain(UcarimaModel ucarima, int cmp, boolean signal, int n) {
        jdplus.ucarima.UcarimaModel ucm = ApiUtility.fromApi(ucarima);
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

    public WienerKolmogorovEstimators wienerKolmogorovEstimators(UcarimaModel ucarima) {
        jdplus.ucarima.UcarimaModel ucm = ApiUtility.fromApi(ucarima);
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

}
