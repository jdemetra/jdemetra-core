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
package ec.tstoolkit.jdr.tdvar;

import static ec.tstoolkit.arima.special.mixedfrequencies.EstimateSpec.Method.Matrix;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TimeVaryingRegressionTest {
    
    public static final double[] FURNITURE = {
        2397, 2465, 2612, 2530, 2609, 2609, 2632, 2643, 2616, 2670, 2773, 3052, 2557, 2451, 2717, 2650,
        2771, 2749, 2812, 2780, 2756, 2827, 3033, 3253, 2540, 2562, 2921, 2829, 2865, 2895, 2930,
        3082, 3070, 3043, 3277, 3548, 2861, 2667, 3057, 2817, 3029, 3050, 3026, 3183, 3195, 3180,
        3422, 3547, 2889, 2964, 3275, 3131, 3283, 3210, 3247, 3363, 3237, 3350, 3624, 3658, 3238,
        3120, 3373, 3255, 3525, 3348, 3402, 3626, 3486, 3610, 3869, 4093, 3574, 3380, 3663, 3474,
        3628, 3612, 3667, 3679, 3609, 3740, 3982, 4174, 3595, 3558, 3947, 3612, 3802, 3823, 3898,
        3969, 3982, 4042, 4309, 4557, 3903, 4078, 4370, 4038, 4262, 4180, 4210, 4346, 4207, 4163,
        4491, 4441, 4058, 3936, 4356, 3892, 4155, 4230, 4151, 4338, 3956, 4136, 4640, 4798, 4102,
        4117, 4454, 4115, 4408, 4155, 4156, 4377, 4054, 4190, 4685, 4529, 4045, 3832, 4290, 3995,
        4381, 4143, 4289, 4534, 4262, 4413, 4695, 4907, 4369, 4338, 4641, 4311, 4462, 4577, 4850,
        4790, 4530, 4688, 5017, 5327, 4448, 4575, 4857, 4615, 4653, 4821, 4880, 5185, 5048, 4925,
        5315, 5476, 4795, 4804, 5154, 4646, 4880, 5004, 4985, 5358, 5127, 4853, 5240, 5269, 4852,
        4904, 5197, 4691, 5023, 4898, 4881, 5214, 4883, 4751, 5077, 5001, 4619, 4685, 4734, 4440,
        4755, 4303, 4471, 4681, 4148, 4031, 4147, 4191, 3893, 3967, 3904, 3626, 3901, 3760, 3865,
        4006, 3895, 3792, 3922, 4021, 3762, 3919, 4183, 3760, 4057, 3835, 4053, 4085, 4016, 3788, 4084, 4120
    };

    public TimeVaryingRegressionTest() {
    }

    @Test
    public void testTD() {
        TsData s=new TsData(new TsPeriod(TsFrequency.Monthly, 1992, 0), FURNITURE, false);
//        long t0=System.currentTimeMillis();
        TimeVaryingRegression.Results regarima = TimeVaryingRegression.regarima(s, "TD7", "Default", 0);
//        System.out.println(regarima.getData("tdeffect", TsData.class));
//        System.out.println(regarima.getData("coefficients.stde", Matrix.class));
//        long t1=System.currentTimeMillis();
//        System.out.println(t1-t0);
        assertTrue(regarima.getData("tdeffect", TsData.class) != null);
    }

}
