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

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.RootMeanSquareNormalizer;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DefaultSeriesScaling implements ISeriesScaling {

    private final Type type;

    public static enum Type {

        MeanAbs,
        RootMeanSquare,
        MaxAbs
    }

    public DefaultSeriesScaling() {
        this.type = Type.MeanAbs;
    }

    public DefaultSeriesScaling(Type type) {
        this.type = type;
    }

    public boolean process(ModellingContext context) {
        if (context.description.getUnits() != 1) {
            return false;
        }
        switch (type) {
            case MeanAbs:
                return calcMeanAbs(context.description);
            case RootMeanSquare:
                return calcRootMeanSquare(context.description);
            case MaxAbs:
                return calcMaxAbs(context.description);
        }
        return false;
    }

    private boolean calcMeanAbs(ModelDescription model) {
        double[] data = model.getY();
        double s = 0;
        int n = 0;
        for (int i = 0; i < data.length; ++i) {
            double d = data[i];
            if (Double.isFinite(d)) {
                s += Math.abs(d);
                ++n;
            }
        }
        if (s == 0) {
            return false;
        }
        model.setUnit(n / s);
        return true;
    }

    private boolean calcRootMeanSquare(ModelDescription model) {
        double[] data = model.getY();
        double s = 0;
        double n = 0;
        for (int i = 0; i < data.length; ++i) {
            double d = data[i];
            if (Double.isFinite(d)) {
                s = RootMeanSquareNormalizer.hypot(s, d);
                n += 1;
            }
        }
        if (s == 0) {
            return false;
        }
        model.setUnit(Math.sqrt(n) / s);
        return true;
    }

    private boolean calcMaxAbs(ModelDescription model) {
        // if (data == null)
        // throw new ArgumentNullException("data");
        double[] data = model.getY();
        double max = 0;
        for (int i = 0; i < data.length; ++i) {
            double d = data[i];
            if (Double.isFinite(d)) {
                d = Math.abs(d);
                if (d > max) {
                    max = d;
                }
            }
        }
        if (max == 0) {
            return false;
        }
        model.setUnit(10 / max);
        return true;
    }
}
