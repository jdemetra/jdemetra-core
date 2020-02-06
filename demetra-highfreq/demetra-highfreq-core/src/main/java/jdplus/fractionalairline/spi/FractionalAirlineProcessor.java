/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.fractionalairline.spi;

import demetra.data.DoubleSeq;
import demetra.highfreq.FractionalAirline;
import demetra.highfreq.FractionalAirlineAlgorithms;
import demetra.highfreq.FractionalAirlineDecomposition;
import demetra.highfreq.FractionalAirlineEstimation;
import demetra.highfreq.FractionalAirlineSpec;
import demetra.modelling.OutlierDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.arima.ArimaModel;
import jdplus.data.DataBlock;
import jdplus.fractionalairline.MultiPeriodicAirlineMapping;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.IOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.SwitchOutlierFactory;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.ami.OutliersDetectionModule;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(FractionalAirlineAlgorithms.Processor.class)
public class FractionalAirlineProcessor implements FractionalAirlineAlgorithms.Processor{

    @Override
    public FractionalAirlineDecomposition process(double[] s, FractionalAirline model) {
        return null;
    }

    @Override
    public FractionalAirlineEstimation process(FractionalAirlineSpec spec) {
        final MultiPeriodicAirlineMapping mapping = new MultiPeriodicAirlineMapping(spec.getPeriodicities(), true, false);
        double[] y=spec.getY();
        RegArimaModel.Builder builder = RegArimaModel.<ArimaModel>builder()
                .y(DoubleSeq.of(y))
                .addX(Matrix.of(spec.getX()))
                .arima(mapping.getDefault())
                .meanCorrection(spec.isMeanCorrection());
        OutlierDescriptor[] o = null;
        if (spec.getOutliers() != null) {
            GlsArimaProcessor<ArimaModel> processor = GlsArimaProcessor.builder(ArimaModel.class)
                    .precision(1e-5)
                    .build();
            IOutlierFactory[] factories = factories(spec.getOutliers());
            OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class)
                    .maxOutliers(100)
                    .addFactories(factories)
                    .processor(processor)
                    .build();
            od.setCriticalValue(spec.getCriticalValue());
            RegArimaModel regarima = builder.build();
            od.prepare(regarima.getObservationsCount());
            od.process(regarima, mapping);
            int[][] io = od.getOutliers();
            o=new OutlierDescriptor[io.length];
            for (int i = 0; i < io.length; ++i) {
                int[] cur = io[i];
                DataBlock xcur = DataBlock.make(y.length);
                factories[cur[1]].fill(cur[0], xcur);
                o[i]=new OutlierDescriptor(factories[cur[1]].getCode(), cur[0]);
                builder.addX(xcur);
            }
        }
        GlsArimaProcessor<ArimaModel> finalProcessor = GlsArimaProcessor.builder(ArimaModel.class)
                .precision(1e-9)
                .build();
        RegArimaEstimation rslt = finalProcessor.process(builder.build(), mapping);
        
        demetra.modelling.regarima.RegArimaEstimation<FractionalAirline> re = jdplus.regarima.ApiUtility.toApi(rslt,s->toApi(spec, mapping, (ArimaModel)s));
        return new FractionalAirlineEstimation(re, o);
        
    }
    
    private static demetra.highfreq.FractionalAirline toApi(FractionalAirlineSpec spec, MultiPeriodicAirlineMapping mapping, ArimaModel arima ){
              double[] parameters = mapping.parametersOf(arima).toArray();
        return new demetra.highfreq.FractionalAirline(spec.getPeriodicities(), parameters, spec.isAdjustToInt());
  
    }

    private static IOutlierFactory[] factories(String[] code) {
        List<IOutlierFactory> fac = new ArrayList<>();
        for (int i = 0; i < code.length; ++i) {
            switch (code[i]) {
                case "ao":
                case "AO":
                    fac.add(AdditiveOutlierFactory.FACTORY);
                    break;
                case "wo":
                case "WO":
                    fac.add(SwitchOutlierFactory.FACTORY);
                    break;
                case "ls":
                case "LS":
                    fac.add(LevelShiftFactory.FACTORY_ZEROENDED);
                    break;
            }
        }
        return fac.toArray(new IOutlierFactory[fac.size()]);
    }
}
