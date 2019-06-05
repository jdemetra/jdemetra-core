/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.timeseries.simplets;

import demetra.timeseries.TsData;
import demetra.timeseries.TsObs;
import demetra.timeseries.TsPeriod;
import jdplus.timeseries.TimeSeriesTransformation;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface TsDataTransformation extends TimeSeriesTransformation<TsPeriod, TsObs, TsData>{
    
    @Override
    TsDataTransformation converse();
}
