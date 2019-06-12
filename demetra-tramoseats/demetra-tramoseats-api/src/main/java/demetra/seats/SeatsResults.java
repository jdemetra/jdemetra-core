/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.seats;

import demetra.arima.UcarimaModel;
import demetra.sa.SeriesDecomposition;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class SeatsResults {

    private SeriesDecomposition initialComponents, finalComponents;
    private UcarimaModel decomposition;
}
