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

import jdplus.arima.ArimaModel;
import jdplus.arima.IArimaModel;
import demetra.data.Data;
import demetra.data.MatrixSerializer;
import jdplus.modelling.regression.AdditiveOutlierFactory;
import jdplus.modelling.regression.LevelShiftFactory;
import jdplus.modelling.regression.SwitchOutlierFactory;
import jdplus.regarima.GlsArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.ami.OutliersDetectionModule;
import jdplus.ucarima.AllSelector;
import jdplus.ucarima.ModelDecomposer;
import jdplus.ucarima.SeasonalSelector;
import jdplus.ucarima.TrendCycleSelector;
import jdplus.ucarima.UcarimaModel;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author Jean Palate
 */
public class MultiPeriodicAirlineMappingTest {

    public MultiPeriodicAirlineMappingTest() {
    }

    //@Test
    public static void testDaily() throws IOException, URISyntaxException {
        URI uri = Data.class.getResource("/edf.txt").toURI();
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

    //@Test
    public static void testDaily2() throws IOException, URISyntaxException {
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
                .addFactory(AdditiveOutlierFactory.FACTORY)
                .addFactory(SwitchOutlierFactory.FACTORY)
                .addFactory(LevelShiftFactory.FACTORY_ZEROSTARTED)
                .maxOutliers(100)
                .processor(processor)
                .build();
        Consumer<int[]> hook=c->{
                    String str=c[0]+"-"+c[1];
                    System.out.println(str);
                };
        od.setAddHook(hook);
        od.setCriticalValue(5);
        od.prepare(regarima.getObservationsCount());
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
    
    public static void main(String[] args) throws IOException, URISyntaxException{
        testDaily();
        testDaily2();
    }
}
            