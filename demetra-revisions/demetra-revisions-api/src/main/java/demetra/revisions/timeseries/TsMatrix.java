/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
