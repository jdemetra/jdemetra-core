/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import jdplus.regarima.tests.OneStepAheadForecastingTest;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class OneStepAheadForecastingTestExtractor extends InformationMapping<OneStepAheadForecastingTest> {

    public static final String FCAST_INSAMPLE_MEAN = "fcast-insample-mean",
            FCAST_OUTSAMPLE_MEAN = "fcast-outsample-mean",
            FCAST_OUTSAMPLE_VARIANCE = "fcast-outsample-variance";

    public OneStepAheadForecastingTestExtractor() {
        set(FCAST_INSAMPLE_MEAN, Double.class, source -> {
            return source.inSampleMeanTest().getPvalue();
        });

        set(FCAST_OUTSAMPLE_MEAN, Double.class, source -> {
            return source.outOfSampleMeanTest().getPvalue();
        });

        set(FCAST_OUTSAMPLE_VARIANCE, Double.class, source -> {
            return source.sameVarianceTest().getPvalue();
        });
    }

    @Override
    public Class getSourceClass() {
        return OneStepAheadForecastingTest.class;
    }
}
