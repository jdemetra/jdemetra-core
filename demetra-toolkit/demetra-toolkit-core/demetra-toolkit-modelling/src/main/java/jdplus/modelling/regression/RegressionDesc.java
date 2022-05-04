/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.modelling.regression;

import demetra.timeseries.regression.ITsVariable;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class RegressionDesc {

    ITsVariable core;
    int item;
    int position;

    double coef, stderr, pvalue;

    public double getTStat() {
        return coef / stderr;
    }
}
