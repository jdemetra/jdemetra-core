/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling;

import demetra.arima.SarimaOrders;
import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.Polynomial;
import jdplus.sarima.SarimaModel;
import jdplus.ucarima.UcarimaModel;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class ApiUtility {
    
    private double[] coefficients(BackFilter filter){
        int n=filter.getDegree();
        double[] w=new double[n];
        for (int i=0; i<n; ++i){
            w[i]=filter.get(i+1);
        }
        return w;
    }
    
    // ARIMA
    public demetra.arima.ArimaModel toApi(IArimaModel model, String name){
        return demetra.arima.ArimaModel.builder()
                .ar(model.getStationaryAr().coefficients())
                .delta(model.getNonStationaryAr().coefficients())
                .ma(model.getMa().coefficients())
                .innovationVariance(model.getInnovationVariance())
                .name(name)
                .build();
    }

    public ArimaModel fromApi(demetra.arima.ArimaModel model){
        return new ArimaModel(
                new BackFilter(Polynomial.raw(model.getAr().toArray())), 
                new BackFilter(Polynomial.raw(model.getDelta().toArray())),
                new BackFilter(Polynomial.raw(model.getMa().toArray())),
                model.getInnovationVariance());
    }
    
    // SARIMA
    public demetra.arima.SarimaModel toApi(SarimaModel model, String name){
        return demetra.arima.SarimaModel.builder()
                .phi(model.getPhi())
                .bphi(model.getBphi())
                .theta(model.getTheta())
                .btheta(model.getBtheta())
                .d(model.getD())
                .bd(model.getBd())
                .name(name)
                .build();
    }

    public SarimaModel fromApi(demetra.arima.SarimaModel model){
        SarimaOrders spec = model.orders();
        return SarimaModel.builder(spec)
                .phi(model.getPhi().toArray())
                .bphi(model.getBphi().toArray())
                .theta(model.getTheta().toArray())
                .btheta(model.getBtheta().toArray())
                .differencing(model.getD(), model.getBd())
                .build();
    }
    
    //UCARIMA
    public demetra.arima.UcarimaModel toApi(UcarimaModel ucm, String[] names){
        
        IArimaModel model = ucm.getModel();
        int n=ucm.getComponentsCount();
        demetra.arima.ArimaModel[] cmps=new demetra.arima.ArimaModel[n];
        for (int i=0; i<n; ++i){
            ArimaModel cmp = ucm.getComponent(i);
            if (cmp != null)
                cmps[i]=toApi(cmp, names == null ? null : names[i]);
        }
        return new demetra.arima.UcarimaModel(model == null ? null : toApi(model, "model"),cmps);
    }

    public UcarimaModel fromApi(demetra.arima.UcarimaModel ucm){
        UcarimaModel.Builder builder = UcarimaModel.builder();
        demetra.arima.ArimaModel model=ucm.getSum();
        if (model != null)
            builder.model(fromApi(model));
        int n=ucm.size();
        for (int i=0; i<n; ++i){
            demetra.arima.ArimaModel cmp = ucm.getComponent(i);
            if (cmp != null)
                builder.add(fromApi(cmp));
        }
        return builder.build();
    }
    
}
