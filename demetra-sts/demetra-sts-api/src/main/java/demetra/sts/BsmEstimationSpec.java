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
package demetra.sts;

import demetra.maths.Optimizer;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public class BsmEstimationSpec {
    public static final double DEF_TOL = 1e-9;
    public static final Optimizer DEF_OPT = Optimizer.LevenbergMarquardt;
    public static final boolean DEF_DREGS = false, DEF_SCALINGFACTOR=true;

    private boolean diffuseRegression = DEF_DREGS;
    private boolean scalingFactor = DEF_SCALINGFACTOR;
    private double precision = DEF_TOL;
    private Optimizer optimizer = DEF_OPT;
  
}
