/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima.r.extensions;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import java.util.function.DoubleUnaryOperator;
import jdplus.arima.AutoCovarianceFunction;
import jdplus.sarima.SarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class SarimaExtension extends InformationMapping<SarimaModel>{

    public SarimaExtension(){
        
        set("spectrum", double[].class, m->
        {
            double[] s=new double[361];
            DoubleUnaryOperator fn = m.getSpectrum().asFunction();
            for (int i=0; i<s.length; ++i){
                s[i]=fn.applyAsDouble(i*Math.PI/360);
            }
            return s;
        });
        set("ac", double[].class, m->
        {
            double[] ac=new double[37];
            AutoCovarianceFunction acf = m.stationaryTransformation().getStationaryModel().getAutoCovarianceFunction();
            for (int i=0; i<ac.length; ++i){
                ac[i]=acf.get(i);
            }
            return ac;
        });
    }
    
    @Override
    public Class<SarimaModel> getSourceClass() {
        return SarimaModel.class;
    }
    
}
