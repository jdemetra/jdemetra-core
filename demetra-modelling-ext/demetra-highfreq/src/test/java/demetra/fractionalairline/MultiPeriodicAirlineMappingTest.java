/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.fractionalairline;

import demetra.arima.ArimaModel;
import demetra.arima.IArimaModel;
import demetra.data.MatrixSerializer;
import demetra.maths.MatrixType;
import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.SwitchOutlier;
import demetra.regarima.GlsArimaProcessor;
import demetra.regarima.RegArimaEstimation;
import demetra.regarima.RegArimaModel;
import demetra.regarima.ami.OutliersDetectionModule;
import demetra.ucarima.AllSelector;
import demetra.ucarima.ModelDecomposer;
import demetra.ucarima.SeasonalSelector;
import demetra.ucarima.TrendCycleSelector;
import demetra.ucarima.UcarimaModel;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class MultiPeriodicAirlineMappingTest {

    public MultiPeriodicAirlineMappingTest() {
    }

    @Test
    public void testDaily() throws IOException, URISyntaxException {
        URI uri = MultiPeriodicAirlineMapping.class.getResource("/edf.txt").toURI();
        MatrixType edf = MatrixSerializer.read(new File(uri));
        final MultiPeriodicAirlineMapping mapping=new MultiPeriodicAirlineMapping(new double[]{7, 365.25}, true, false);
        GlsArimaProcessor<ArimaModel> processor=GlsArimaProcessor.builder(ArimaModel.class)
                .mapping(mapping)
                .precision(1e-5)
                .build();
        RegArimaModel<ArimaModel> regarima=RegArimaModel.builder(ArimaModel.class)
                .y(edf.column(0))
                .arima(mapping.getDefault())
                .build();
        RegArimaEstimation<ArimaModel> estimation = processor.process(regarima);
        assertTrue(estimation != null);
        UcarimaModel ucm=ucm(estimation.getModel().arima(), true);
        assertTrue(ucm.isValid());
//        System.out.println(ucm);
//        System.out.println(ucm.getComponent(2).getMA().asPolynomial().coefficients());
    }

    @Test
    public void testDaily2() throws IOException, URISyntaxException {
        URI uri = MultiPeriodicAirlineMapping.class.getResource("/edf.txt").toURI();
        MatrixType edf = MatrixSerializer.read(new File(uri));
        final MultiPeriodicAirlineMapping mapping=new MultiPeriodicAirlineMapping(new double[]{7, 365}, true, false);
        GlsArimaProcessor<ArimaModel> processor=GlsArimaProcessor.builder(ArimaModel.class)
                .mapping(mapping )
                .precision(1e-5)
                .build();
        RegArimaModel<ArimaModel> regarima=RegArimaModel.builder(ArimaModel.class)
                .y(edf.column(0))
                .arima(mapping.getDefault())
                .build();
        RegArimaEstimation<ArimaModel> estimation = processor.process(regarima);
        assertTrue(estimation != null);
        UcarimaModel ucm=ucm(estimation.getModel().arima(), true);
        assertTrue(ucm.isValid());
//        System.out.println(ucm);
//        System.out.println(ucm.getComponent(2).getMA().asPolynomial().coefficients());
    }
    
    @Test
    @Ignore
    public void testOutliers() throws IOException, URISyntaxException {
        URI uri = MultiPeriodicAirlineMapping.class.getResource("/births.txt").toURI();
        MatrixType edf = MatrixSerializer.read(new File(uri));
        final MultiPeriodicAirlineMapping mapping=new MultiPeriodicAirlineMapping(new double[]{7, 365.25}, true, false);
        GlsArimaProcessor<ArimaModel> processor=GlsArimaProcessor.builder(ArimaModel.class)
                .mapping(mapping )
                .precision(1e-5)
                .build();
        RegArimaModel<ArimaModel> regarima=RegArimaModel.builder(ArimaModel.class)
                .y(edf.column(0).range(5000, 8000))
                .arima(mapping.getDefault())
                .build();
        OutliersDetectionModule od = OutliersDetectionModule.build(ArimaModel.class)
                .addFactory(AdditiveOutlier.FACTORY)
                //.addFactory(SwitchOutlier.FACTORY)
                //.addFactory(LevelShift.FACTORY_ZEROSTARTED)
                .maxOutliers(100)
                .processor(processor)
                .build();
        Consumer<int[]> hook=c->{
                    String str=c[0]+"-"+c[1];
                    System.out.println(str);
                };
        od.setAddHook(hook);
        od.setCriticalValue(6);
        od.process(regarima);
    }
    
    public static UcarimaModel ucm(IArimaModel arima, boolean week) {

        TrendCycleSelector tsel = new TrendCycleSelector();
        AllSelector ssel = new AllSelector();

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        if (week)
            decomposer.add(new SeasonalSelector(7));
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(arima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }
}
            