/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.r;

import demetra.arima.ArimaModel;
import demetra.data.DoubleSeq;
import java.util.function.DoubleUnaryOperator;
import jdplus.arima.AutoCovarianceFunction;
import jdplus.arima.Spectrum;
import jdplus.modelling.ApiUtility;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ArimaModels {

    public ArimaModel of(String name, double[] ar, double[] delta, double[] ma, double variance) {
        return ArimaModel.builder()
                .name(name)
                .ar(ar == null ? DoubleSeq.ONE : DoubleSeq.of(ar))
                .delta(delta == null ? DoubleSeq.ONE : DoubleSeq.of(delta))
                .ma(ma == null ? DoubleSeq.ONE : DoubleSeq.of(ma))
                .innovationVariance(variance)
                .build();
    }

    public ArimaModel sum(ArimaModel[] components) {
        if (components == null || components.length == 0) {
            return null;
        }
        if (components.length == 1) {
            return components[0];
        }

        jdplus.arima.ArimaModel m = ApiUtility.fromApi(components[0]);
        for (int i = 1; i < components.length; ++i) {
            m = m.plus(ApiUtility.fromApi(components[i]));
        }
        return ApiUtility.toApi(m, "sum");
    }

    public double[] spectrum(ArimaModel arima, int n) {
        jdplus.arima.ArimaModel m = ApiUtility.fromApi(arima);
        DoubleUnaryOperator s = m.getSpectrum().asFunction();
        double[] g = new double[n];
        double q = Math.PI / (n - 1);
        for (int i = 0; i < n; ++i) {
            double w = q * i;
            g[i] = s.applyAsDouble(w);
        }
        return g;
    }

    public double[] acf(ArimaModel arima, int n) {
        jdplus.arima.ArimaModel m = ApiUtility.fromApi(arima);
        AutoCovarianceFunction acf = m.stationaryTransformation().getStationaryModel().getAutoCovarianceFunction();
        acf.prepare(n);
        double[] g = new double[n+1];
        for (int i = 0; i <= n; ++i) {
            g[i] = acf.get(i);
        }
        return g;
    }

}
