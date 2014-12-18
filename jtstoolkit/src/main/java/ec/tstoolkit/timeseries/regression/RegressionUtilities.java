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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class RegressionUtilities {

    public static List<DataBlock> data(ITsVariable var, TsDomain domain) {
        ArrayList<DataBlock> cols = new ArrayList<>();
        int dim = var.getDim();
        for (int i = 0; i < dim; ++i) {
            cols.add(new DataBlock(domain.getLength()));
        }
        var.data(domain, cols);
        return cols;
    }

    public static Matrix matrix(ITsVariable var, TsDomain domain) {
        Matrix m = new Matrix(domain.getLength(), var.getDim());
        var.data(domain, m.columnList());
        return m;
    }

    public static TsData toTsData(ITsVariable var, TsDomain sdom) {
        if (var.getDim() != 1) {
            return null;
        }
        if (sdom == null) {
            TsDomain d = var.getDefinitionDomain();
            if (d != null) {
                List<DataBlock> data = Collections.singletonList(new DataBlock(d.getLength()));
                var.data(d, data);
                return new TsData(d.getStart(), data.get(0));
            } else {
                return null;
            }
        } else {
            TsFrequency dfreq = var.getDefinitionFrequency();
            TsDomain ddom = var.getDefinitionDomain();
            if (dfreq != TsFrequency.Undefined && dfreq != sdom.getFrequency()) {
                return null;
            }

            if (ddom == null || ddom.contains(sdom)) {
                List<DataBlock> data = Collections.singletonList(new DataBlock(sdom.getLength()));
                var.data(sdom, data);
                return new TsData(sdom.getStart(), data.get(0));
            } else {
                return null;
            }
        }
    }
}
