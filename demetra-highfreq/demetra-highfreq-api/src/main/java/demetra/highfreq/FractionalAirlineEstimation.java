/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.modelling.OutlierDescriptor;
import demetra.modelling.regarima.RegArimaEstimation;

/**
 *
 * @author palatej
 */
@lombok.Value
public class FractionalAirlineEstimation {
        RegArimaEstimation<FractionalAirline> regarima;
//        LinearModelDescription model;
        OutlierDescriptor[] outliers;
}
