/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.r;

import demetra.arima.SarimaOrders;
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
    
    public demetra.arima.SarimaModel of(String name, int period, double[] phi, int d, double[] theta, double[] bphi, int bd, double[] btheta) {
        SarimaOrders orders=new SarimaOrders(period);
        orders.setRegular(phi == null ? 0 : phi.length, d, theta == null ? 0 : theta.length);
        orders.setSeasonal(bphi == null ? 0 : bphi.length, bd, btheta == null ? 0 : btheta.length);
        return demetra.arima.SarimaModel.builder()
                .name(name)
                .period(period)
                .phi(phi == null ? DoubleSeq.empty() : DoubleSeq.of(phi))
                .d(d)
                .theta(theta == null ? DoubleSeq.empty() : DoubleSeq.of(theta))
                .bphi(bphi == null ? DoubleSeq.empty() : DoubleSeq.of(bphi))
                .bd(bd)
                .btheta(btheta == null ? DoubleSeq.empty() : DoubleSeq.of(btheta))
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
                .setPeriod(model.getPeriod())
                .setD(model.getD())
                .setBd(model.getBd());
        
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
