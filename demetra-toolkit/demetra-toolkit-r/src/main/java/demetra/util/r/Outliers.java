/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.util.r;

import demetra.data.DoubleSeq;
import jdplus.stats.RobustStandardDeviationComputer;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Outliers {
    double MAD(double[] data, double centile){
        return RobustStandardDeviationComputer.mad(centile, true).compute(DoubleSeq.of(data));
    }
}
