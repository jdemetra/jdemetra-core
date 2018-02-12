/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.tramo;

import demetra.data.DoubleSequence;
import demetra.design.Development;
import static demetra.maths.Optimizer.LevenbergMarquardt;
import demetra.maths.functions.levmar.LevenbergMarquardtMinimizer;
import demetra.maths.functions.minpack.MinPackMinimizer;
import demetra.maths.functions.ssq.ISsqFunctionMinimizer;
import demetra.regarima.IRegArimaProcessor;
import demetra.regarima.RegArimaModel;
import demetra.sarima.GlsSarimaProcessor;
import demetra.sarima.SarimaModel;
import demetra.sarima.SarimaSpecification;
import demetra.sarima.internal.HannanRissanenInitializer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.experimental.UtilityClass
public class TramoUtility {

}
