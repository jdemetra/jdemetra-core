/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.r;

import demetra.data.DoubleSeq;
import demetra.arima.SarimaSpec;
import demetra.modelling.io.protobuf.ModellingProtos;
import jdplus.arima.ArimaSeriesGenerator;
import jdplus.dstats.Normal;
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
    
    public double[] random(int length, int period, double[] phi, int d, double[] theta, double[] bphi, int bd, double[] btheta, double stde) {
        SarimaModel sarima = SarimaModel.builder(period)
                .differencing(d, bd)
                .phi(phi)
                .theta(theta)
                .bphi(bphi)
                .btheta(btheta)
                .build();
        ArimaSeriesGenerator generator = ArimaSeriesGenerator.builder()
                .distribution(new Normal(0, stde))
                .build();
        return generator.generate(sarima, length);
    }
    
    public SarimaModel estimate(double[] data, int[] regular, int period, int[] seasonal, double[] parameters){
        SarimaSpec.Builder builder = SarimaSpec.builder()
                .period(period)
                .p(regular[0])
                .d(regular[1])
                .q(regular[2]);
        if (seasonal != null){
            builder
                .bp(seasonal[0])
                .bd(seasonal[1])
                .bq(seasonal[2]);
        }
        // TODO. Fix parameters, if any
        if (parameters != null){
                        
        }
        SarimaSpec sarima = builder.build();
        
        RegArimaModel<SarimaModel> regarima=RegArimaModel.<SarimaModel>builder()
                .arima(SarimaModel.builder(sarima).build())
                .y(DoubleSeq.of(data))
                .build();
         RegSarimaComputer processor = RegSarimaComputer.builder()
                .startingPoint(RegSarimaComputer.StartingPoint.HannanRissanen)
                .build();
        RegArimaEstimation<SarimaModel> rslt = processor.process(regarima, SarimaMapping.of(sarima.orders()));
        return rslt.getModel().arima();
    }
    
    public byte[] toBuffer(SarimaModel model){
        ModellingProtos.SarimaModel.Builder builder = ModellingProtos.SarimaModel.newBuilder()
                .setName("sarima")
                .setPeriod(model.getPeriod())
                .setD(model.getD())
                .setBd(model.getBd())
                ;
        
        for (int i=1; i<=model.getP(); ++i){
            builder.addPhi(model.phi(i));
        }
        for (int i=1; i<=model.getBp(); ++i){
            builder.addBphi(model.bphi(i));
        }
        for (int i=1; i<=model.getQ(); ++i){
            builder.addTheta(model.theta(i));
        }
        for (int i=1; i<=model.getBq(); ++i){
            builder.addBtheta(model.btheta(i));
        }
        return builder.build().toByteArray();      
    }
    
   
}
