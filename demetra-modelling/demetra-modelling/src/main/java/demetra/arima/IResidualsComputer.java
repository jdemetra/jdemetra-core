/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.arima;

import demetra.arima.internal.KalmanFilter;
import demetra.arima.internal.ModifiedLjungBoxFilter;
import demetra.data.DataBlock;
import demetra.data.DoubleSequence;


/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IResidualsComputer {

    DoubleSequence residuals(IArimaModel arma, DoubleSequence y);

    public static IResidualsComputer mlComputer() {
        return (arma, y) -> {
            ModifiedLjungBoxFilter f = new ModifiedLjungBoxFilter();
            int n = y.length();
            int nf = f.prepare(arma, n);
            DataBlock fres = DataBlock.make(nf);
            f.apply(y, fres);
            return nf == n ? fres : fres.drop(nf - n, 0);
        };
    }
    
    public static IResidualsComputer defaultComputer() {
        return defaultComputer(new KalmanFilter(false));
    }
   
    public static IResidualsComputer defaultComputer(final IArmaFilter filter) {
        return (arma, y) -> {
            int n = y.length();
            int nf = filter.prepare(arma, n);
            DataBlock fres = DataBlock.make(nf);
            filter.apply(y, fres);
            return fres;
        };
    }
}
