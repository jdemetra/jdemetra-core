/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.arima.estimation;

import jdplus.arima.IArimaModel;
import internal.jdplus.arima.KalmanFilter;
import internal.jdplus.arima.ModifiedLjungBoxFilter;
import jdplus.data.DataBlock;
import jdplus.arima.estimation.ArmaFilter;
import demetra.data.DoubleSeq;


/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface ResidualsComputer {

    DoubleSeq residuals(IArimaModel arma, DoubleSeq y);

    public static ResidualsComputer mlComputer() {
        return (arma, y) -> {
            ModifiedLjungBoxFilter f = new ModifiedLjungBoxFilter();
            int n = y.length();
            int nf = f.prepare(arma, n);
            DataBlock fres = DataBlock.make(nf);
            f.apply(y, fres);
            return nf == n ? fres : fres.drop(nf - n, 0);
        };
    }
    
    public static ResidualsComputer defaultComputer() {
        return defaultComputer(new KalmanFilter(false));
    }
   
    public static ResidualsComputer defaultComputer(final ArmaFilter filter) {
        return (arma, y) -> {
            int n = y.length();
            int nf = filter.prepare(arma, n);
            DataBlock fres = DataBlock.make(nf);
            filter.apply(y, fres);
            return fres;
        };
    }
}
