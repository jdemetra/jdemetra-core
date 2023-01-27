/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.regarima;

import demetra.timeseries.TsData;

/**
 *
 * @author palatej
 */
class Converter {
    static ec.tstoolkit.timeseries.simplets.TsData convert(TsData s) {
        int period = s.getAnnualFrequency();
        int year = s.getStart().year(), pos = s.getStart().annualPosition();
        return new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.valueOf(period),
                year, pos, s.getValues().toArray(), false);
    }
    
}
