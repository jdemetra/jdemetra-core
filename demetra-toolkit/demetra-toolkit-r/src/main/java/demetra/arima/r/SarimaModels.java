/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.r;

import demetra.data.DoubleSeq;
import demetra.arima.SarimaSpec;
import demetra.math.matrices.Matrix;
import demetra.modelling.io.protobuf.ModellingProtos;
import demetra.regarima.io.protobuf.RegArimaEstimationProto;
import demetra.regarima.io.protobuf.RegArimaProtos;
import demetra.regarima.io.protobuf.RegArimaProtosUtility;
import java.util.function.DoubleUnaryOperator;
import jdplus.arima.ArimaModel;
import jdplus.arima.ArimaSeriesGenerator;
import jdplus.arima.AutoCovarianceFunction;
import jdplus.dstats.Normal;
import jdplus.dstats.T;
import jdplus.math.matrices.FastMatrix;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regsarima.RegSarimaComputer;
import jdplus.sarima.SarimaModel;
import jdplus.sarima.estimation.SarimaMapping;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SarimaModels {

    public SarimaModel of(int period, double[] phi, int d, double[] theta, double[] bphi, int bd, double[] btheta) {
        return SarimaModel.builder(period)
                .phi(phi)
                .differencing(d, bd)
                .theta(theta)
                .bphi(bphi)
                .btheta(btheta)
                .build();
    }

    /**
     * Generates random series for a given Sarima model with either Normal
     * noises or T noises
     *
     * @param length
     * @param period
     * @param phi
     * @param d
     * @param theta
     * @param bphi
     * @param bd
     * @param btheta
     * @param stde
     * @param tdegree Degrees of T-Stat. O if normal is used
     * @return
     */
    public double[] random(int length, int period, double[] phi, int d, double[] theta, double[] bphi, int bd, double[] btheta, double stde, int tdegree) {
        if (stde == 0) {
            stde = 1;
        }
        SarimaModel sarima = SarimaModel.builder(period)
                .differencing(d, bd)
                .phi(phi)
                .theta(theta)
                .bphi(bphi)
                .btheta(btheta)
                .build();
        ArimaSeriesGenerator generator = ArimaSeriesGenerator.builder()
                .distribution(tdegree <= 0 ? new Normal(0, stde) : new T(tdegree))
                .startMean(10 * stde)
                .startStdev(stde)
                .build();
        return generator.generate(sarima, length);
    }

    public RegArimaEstimation<SarimaModel> estimate(double[] data, int[] regular, int period, int[] seasonal, boolean mean, Matrix X, double[] parameters, double eps) {
        SarimaSpec.Builder builder = SarimaSpec.builder()
                .period(period)
                .p(regular[0])
                .d(regular[1])
                .q(regular[2]);
        if (seasonal != null) {
            builder
                    .bp(seasonal[0])
                    .bd(seasonal[1])
                    .bq(seasonal[2]);
        }
        
        // TODO. Fix parameters, if any
        if (parameters != null) {

        }
        SarimaSpec sarima = builder.build();

        RegArimaModel<SarimaModel> regarima = RegArimaModel.<SarimaModel>builder()
                .arima(SarimaModel.builder(sarima).build())
                .y(DoubleSeq.of(data))
                .meanCorrection(mean)
                .addX(FastMatrix.of(X))
                .build();
        RegSarimaComputer processor = RegSarimaComputer.builder()
                .startingPoint(RegSarimaComputer.StartingPoint.HannanRissanen)
                .computeExactFinalDerivatives(true)
                .useParallelProcessing(true)
                .precision(eps)
                .build();
        RegArimaEstimation<SarimaModel> rslt = processor.process(regarima, SarimaMapping.of(sarima.orders()));
        return rslt;
    }

    public double[] spectrum(SarimaModel m, int n) {
        DoubleUnaryOperator s = m.getSpectrum().asFunction();
        double[] g = new double[n];
        double q = Math.PI / (n - 1);
        for (int i = 0; i < n; ++i) {
            double w = q * i;
            g[i] = s.applyAsDouble(w);
        }
        return g;
    }
    
    public ArimaModel convert(SarimaModel model){
        return ArimaModel.of(model);
    }

    public double[] acf(SarimaModel m, int n) {
        AutoCovarianceFunction acf = m.stationaryTransformation().getStationaryModel().getAutoCovarianceFunction();
        acf.prepare(n);
        double[] g = new double[n + 1];
        for (int i = 0; i <= n; ++i) {
            g[i] = acf.get(i);
        }
        return g;
    }

    public byte[] toBuffer(SarimaModel model) {
        ModellingProtos.SarimaModel.Builder builder = ModellingProtos.SarimaModel.newBuilder()
                .setName("sarima")
                .setPeriod(model.getPeriod())
                .setD(model.getD())
                .setBd(model.getBd());

        for (int i = 1; i <= model.getP(); ++i) {
            builder.addPhi(model.phi(i));
        }
        for (int i = 1; i <= model.getBp(); ++i) {
            builder.addBphi(model.bphi(i));
        }
        for (int i = 1; i <= model.getQ(); ++i) {
            builder.addTheta(model.theta(i));
        }
        for (int i = 1; i <= model.getBq(); ++i) {
            builder.addBtheta(model.btheta(i));
        }
        return builder.build().toByteArray();
    }

    public byte[] toBuffer(RegArimaEstimation<SarimaModel> regarima) {
        RegArimaProtos.RegArimaModel.Estimation estimation = RegArimaEstimationProto.convert(regarima);
        return estimation.toByteArray();
    }

}
