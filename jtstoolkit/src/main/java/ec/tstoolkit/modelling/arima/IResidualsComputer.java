/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.IArmaFilter;
import ec.tstoolkit.arima.estimation.KalmanFilter;
import ec.tstoolkit.arima.estimation.ModifiedLjungBoxFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface IResidualsComputer {

    DataBlock residuals(IArimaModel arma, IReadDataBlock y);

    public static IResidualsComputer mlComputer() {
        return (arma, y) -> {
            ModifiedLjungBoxFilter f = new ModifiedLjungBoxFilter();
            int n = y.getLength();
            int nf = f.initialize(arma, n);
            DataBlock fres = new DataBlock(nf);
            f.filter(y, fres);
            return nf == n ? fres : fres.drop(nf - n, 0);
        };
    }
    
    public static IResidualsComputer defaultComputer() {
        return defaultComputer(new KalmanFilter(false));
    }
   
    public static IResidualsComputer defaultComputer(final IArmaFilter filter) {
        return (arma, y) -> {
            int n = y.getLength();
            int nf = filter.initialize(arma, n);
            DataBlock fres = new DataBlock(nf);
            filter.filter(y, fres);
            return fres;
        };
    }
}
