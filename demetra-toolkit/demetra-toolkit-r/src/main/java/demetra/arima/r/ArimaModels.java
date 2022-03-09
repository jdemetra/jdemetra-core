/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.r;

import demetra.data.Iterables;
import demetra.math.Complex;
import demetra.modelling.io.protobuf.ModellingProtos;
import java.util.function.DoubleUnaryOperator;
import jdplus.arima.ArimaModel;
import jdplus.arima.AutoCovarianceFunction;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.sarima.estimation.SarimaMapping;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ArimaModels {

    public ArimaModel of(double[] ar, double[] delta, double[] ma, double variance, boolean check) {
        Polynomial AR = ar == null ? Polynomial.ONE : Polynomial.of(ar);
        Polynomial D = delta == null ? Polynomial.ONE : Polynomial.of(delta);
        Polynomial MA = ma == null ? Polynomial.ONE : Polynomial.of(ma);

        if (check) {
            if (!SarimaMapping.checkStability(AR.coefficients())) {
                throw new IllegalArgumentException("AR");
            }
            if (!SarimaMapping.checkStability(MA.coefficients())) {
                throw new IllegalArgumentException("MA");
            }
            if (D.degree() > 0) {
                Complex[] roots = D.roots();
                for (int i = 0; i < roots.length; ++i) {
                    if (Math.abs(1 - roots[i].absSquare()) > 1e-6) {
                        throw new IllegalArgumentException("DELTA");
                    }
                }
            }
        }
        return new ArimaModel(
                new BackFilter(AR), new BackFilter(D), new BackFilter(MA), variance);
    }

    public ArimaModel sum(ArimaModel[] components) {
        if (components == null || components.length == 0) {
            return null;
        }
        if (components.length == 1) {
            return components[0];
        }

        jdplus.arima.ArimaModel m = components[0];
        for (int i = 1; i < components.length; ++i) {
            m = m.plus(components[i]);
        }
        return m;
    }

    public double[] spectrum(ArimaModel m, int n) {
        DoubleUnaryOperator s = m.getSpectrum().asFunction();
        double[] g = new double[n];
        double q = Math.PI / (n - 1);
        for (int i = 0; i < n; ++i) {
            double w = q * i;
            g[i] = s.applyAsDouble(w);
        }
        return g;
    }

    public double[] acf(ArimaModel m, int n) {
        AutoCovarianceFunction acf = m.stationaryTransformation().getStationaryModel().getAutoCovarianceFunction();
        acf.prepare(n);
        double[] g = new double[n + 1];
        for (int i = 0; i <= n; ++i) {
            g[i] = acf.get(i);
        }
        return g;
    }

    public byte[] toBuffer(ArimaModel model) {
        ModellingProtos.ArimaModel.Builder builder = ModellingProtos.ArimaModel.newBuilder()
                .setName("arima")
                .addAllAr(Iterables.of(model.getStationaryAr().coefficients()))
                .addAllDelta(Iterables.of(model.getNonStationaryAr().coefficients()))
                .addAllMa(Iterables.of(model.getMa().coefficients()))
                .setInnovationVariance(model.getInnovationVariance());

        return builder.build().toByteArray();
    }
}
