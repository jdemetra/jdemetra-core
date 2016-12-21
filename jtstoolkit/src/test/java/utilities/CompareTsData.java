/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Christiane Hofer
 */
public class CompareTsData {

    public static boolean compareTS(TsData orignal, TsData test, double precision) {
        if (!orignal.getStart().equals(test.getStart())) {
            return false;
        }
        if (!(orignal.getLength() == test.getLength())) {
            return false;
        }
        for (int i = 0; i < orignal.getLength(); i++) {
            if (Math.abs(orignal.get(i) - test.get(i)) > precision) {
                return false;
            }
        }
        return true;
    }
    
}
