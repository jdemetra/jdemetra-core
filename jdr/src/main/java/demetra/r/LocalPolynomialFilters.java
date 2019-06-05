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
package demetra.r;

import jdplus.maths.linearfilters.FiniteFilter;
import jdplus.maths.linearfilters.SymmetricFilter;
import jdplus.data.analysis.DiscreteKernel;
import java.util.function.IntToDoubleFunction;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class LocalPolynomialFilters {

    public double[] filter(double[] data, int horizon, int degree, String kernel, String endpoints, double ic) {
        // Creates the filters
        IntToDoubleFunction weights = weights(horizon, kernel);
        SymmetricFilter filter = jdplus.maths.linearfilters.LocalPolynomialFilters.of(horizon, degree, weights);
        FiniteFilter[] afilters;
        if (endpoints.equals("DAF")) {
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = jdplus.maths.linearfilters.LocalPolynomialFilters.directAsymmetricFilter(horizon, i, degree, weights);
            }
        } else if (endpoints.equals("CN")) {
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = jdplus.maths.linearfilters.LocalPolynomialFilters.cutAndNormalizeFilter(filter, i);
            }
        } else {
            int u = 0;
            double[] c = new double[]{ic};
            switch (endpoints) {
                case "CC":
                    c = new double[0];
                case "LC":
                    u = 0;
                    break;
                case "QL":
                    u = 1;
                    break;
                case "CQ":
                    u = 2;
                    break;
            }
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = jdplus.maths.linearfilters.LocalPolynomialFilters.asymmetricFilter(filter, i, u, c, null);
            }
        }

        DoubleSeq rslt = jdplus.maths.linearfilters.LocalPolynomialFilters.filter(DoubleSeq.of(data), filter, afilters);
        return rslt.toArray();
    }

    IntToDoubleFunction weights(int horizon, String filter) {
        switch (filter) {
            case "Uniform":
                return DiscreteKernel.uniform(horizon);
            case "Biweight":
                return DiscreteKernel.biweight(horizon);
            case "Triweight":
                return DiscreteKernel.triweight(horizon);
            case "Tricube":
                return DiscreteKernel.tricube(horizon);
            case "Triangular":
                return DiscreteKernel.triangular(horizon);
            case "Parabolic":
                return DiscreteKernel.parabolic(horizon);
            case "Gaussian":
                return DiscreteKernel.gaussian(4 * horizon);
            default:
                return DiscreteKernel.henderson(horizon);
        }
    }

    public FiltersToolkit.FiniteFilters filterProperties(int horizon, int degree, String kernel, String endpoints, double ic) {
        // Creates the filters
        IntToDoubleFunction weights = weights(horizon, kernel);
        SymmetricFilter filter = jdplus.maths.linearfilters.LocalPolynomialFilters.of(horizon, degree, weights);
        FiniteFilter[] afilters;
        if (endpoints.equals("DAF")) {
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = jdplus.maths.linearfilters.LocalPolynomialFilters.directAsymmetricFilter(horizon, i, degree, weights);
            }
        } else if (endpoints.equals("CN")) {
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = jdplus.maths.linearfilters.LocalPolynomialFilters.cutAndNormalizeFilter(filter, i);
            }
        } else {
            int u = 0;
            double[] c = new double[]{ic};
            switch (endpoints) {
                case "CC":
                    c = new double[0];
                case "LC":
                    u = 0;
                    break;
                case "QL":
                    u = 1;
                    break;
                case "CQ":
                    u = 2;
                    break;
            }
            afilters = new FiniteFilter[horizon];
            for (int i = 0; i < afilters.length; ++i) {
                afilters[i] = jdplus.maths.linearfilters.LocalPolynomialFilters.asymmetricFilter(filter, i, u, c, null);
            }
        }
        return new FiltersToolkit.FiniteFilters(filter, afilters);

    }

}
