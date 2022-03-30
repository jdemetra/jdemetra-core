/*
 * Copyright 2022 National Bank of Belgium
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
package jdplus.highfreq;

import demetra.data.MatrixSerializer;
import demetra.highfreq.DecompositionSpec;
import demetra.highfreq.ExtendedAirlineDecompositionSpec;
import demetra.highfreq.ExtendedAirlineModellingSpec;
import demetra.highfreq.ExtendedAirlineSpec;
import demetra.highfreq.HolidaysSpec;
import demetra.highfreq.OutlierSpec;
import demetra.highfreq.RegressionSpec;
import demetra.highfreq.TransformSpec;
import demetra.math.matrices.Matrix;
import demetra.modelling.ComponentInformation;
import demetra.modelling.TransformationType;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.HolidaysOption;
import demetra.timeseries.regression.ModellingContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static jdplus.highfreq.ExtendedAirlineKernelTest.france;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class ExtendedAirlineDecompositionKernelTest {
    
    final static TsData EDF;

    static {
        TsData y;
        try {
            InputStream stream = ExtendedAirlineMapping.class.getResourceAsStream("/edf.txt");
            Matrix edf = MatrixSerializer.read(stream);
            y = TsData.of(TsPeriod.daily(1996, 1, 1), edf.column(0));
        } catch (IOException ex) {
            y = null;
        }
        EDF = y;
    }
    
    public ExtendedAirlineDecompositionKernelTest() {
    }

    public static void main(String[] args){
        testEDF();
    }

    public static void testEDF() {
        Holiday[] france = france();
        ModellingContext context=new ModellingContext();
        context.getCalendars().set("FR", new Calendar(france));
       // build the psec
        ExtendedAirlineModellingSpec spec=ExtendedAirlineModellingSpec.builder()
                .transform(TransformSpec.builder()
                        .function(TransformationType.Log)
                        .build())
                .stochastic(ExtendedAirlineSpec.DEFAULT_WD)
                .outlier(OutlierSpec.builder()
                        .criticalValue(6)
                        .outliers(new String[]{"AO"})
                        .build())
                .regression(RegressionSpec.builder()
                        .calendar(HolidaysSpec.builder()
                                        .holidays("FR")
                                        .holidaysOption(HolidaysOption.Skip)
                                        .single(false)
                                        .build())
                        .build())
                .build();
        
        DecompositionSpec dspec = DecompositionSpec.builder()
                .periodicities(new double[]{7, 365.25})
                .build();
        ExtendedAirlineDecompositionSpec allspec = ExtendedAirlineDecompositionSpec.builder()
                .preprocessing(spec)
                .decomposition(dspec)
                .build();
        ExtendedAirlineDecompositionKernel kernel=new ExtendedAirlineDecompositionKernel(allspec, context);
        ExtendedAirlineResults rslts = kernel.process(EDF, null);
        List<TsData> main=new ArrayList<>();
        main.add(rslts.getFinals().getSeries(ComponentType.Series, ComponentInformation.Value));
        main.add(rslts.getFinals().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value));
        main.add(rslts.getFinals().getSeries(ComponentType.Trend, ComponentInformation.Value));
        main.add(rslts.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
        main.add(rslts.getFinals().getSeries(ComponentType.Irregular, ComponentInformation.Value));
        System.out.println(TsDataTable.of(main));
    }
    
}
