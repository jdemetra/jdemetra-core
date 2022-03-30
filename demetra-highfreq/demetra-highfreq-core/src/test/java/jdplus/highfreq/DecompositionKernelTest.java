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

import demetra.data.DoubleSeq;
import demetra.data.MatrixSerializer;
import demetra.highfreq.DecompositionSpec;
import demetra.math.matrices.Matrix;
import demetra.modelling.ComponentInformation;
import demetra.processing.ProcessingLog;
import demetra.sa.ComponentType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class DecompositionKernelTest {

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

    public DecompositionKernelTest() {
    }

    @Test
    public void testSimple() {
        DecompositionSpec spec = DecompositionSpec.builder()
                .periodicities(new double[]{7})
                .stdev(true)
                .forecastsCount(35)
                .build();
        DecompositionKernel kernel = new DecompositionKernel(spec);
        ExtendedAirlineDecomposition decomp = kernel.process(EDF.log(), true, ProcessingLog.dummy());
        assertTrue(decomp != null);
    }

    public static void main(String[] args){
        testComplex();
    }
        
        
    public static void testComplex() {
        DecompositionSpec spec = DecompositionSpec.builder()
                .stdev(false)
                .biasCorrection(false)
                .forecastsCount(35)
                .periodicities(new double[]{7, 365.25})
                .build();
        DecompositionKernel kernel = new DecompositionKernel(spec);
        ExtendedAirlineDecomposition decomp = kernel.process(EDF.log(), true, ProcessingLog.dummy());
        System.out.println(decomp.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value));
    }
}
