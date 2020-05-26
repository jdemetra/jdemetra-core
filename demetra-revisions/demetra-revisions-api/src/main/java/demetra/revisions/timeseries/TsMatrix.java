/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.revisions.timeseries;

import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDataTable;
import demetra.timeseries.TsDataTable.ValueStatus;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.util.Arrays;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class TsMatrix {

    @lombok.NonNull
    private TsPeriod start;
    @lombok.NonNull
    private MatrixType matrix;

    public TsDomain domain() {
        return TsDomain.of(start, matrix.getRowsCount());
    }

    /**
     * Creates a new time series matrix
     *
     * @param s The array of time series that will constitute the matrix.
     * The time domain of the different series may differ.
     */
    public static TsMatrix of(TsData... s) {
        TsDataTable tmp = TsDataTable.of(Arrays.asList(s));
        TsDomain domain = tmp.getDomain();
        TsPeriod start = domain.getStartPeriod();
        double[] x = new double[domain.getLength() * s.length];
        TsDataTable.Cursor cursor = tmp.cursor(TsDataTable.DistributionType.LAST);

        for (int j = 0, k = 0; j < s.length; ++j) {
            for (int i = 0; i < domain.getLength(); ++i, ++k) {
                if (cursor.moveTo(i, j).getStatus() == ValueStatus.PRESENT) {
                    x[k] = cursor.getValue();
                } else {
                    x[k] = Double.NaN;
                }
            }
        }
        return new TsMatrix(start, MatrixType.of(x, domain.getLength(), s.length));
    }
}
