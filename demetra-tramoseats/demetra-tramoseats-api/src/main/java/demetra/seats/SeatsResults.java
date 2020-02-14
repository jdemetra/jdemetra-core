/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.seats;

import demetra.arima.SarimaModel;
import demetra.arima.UcarimaModel;
import demetra.information.InformationSet;
import demetra.sa.SeriesDecomposition;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class SeatsResults {

    private SarimaModel initialModel, finalModel;
    private SeriesDecomposition initialComponents, finalComponents;
    private UcarimaModel decomposition;
    private int backcastsCount, forecastsCount;
    
    private InformationSet addtionalResults=new InformationSet();

    
}
