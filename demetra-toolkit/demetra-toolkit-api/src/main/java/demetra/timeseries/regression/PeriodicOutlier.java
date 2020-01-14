/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import java.time.LocalDateTime;

/**
 *
 * @author palatej
 */
@lombok.Value
public class PeriodicOutlier implements IOutlier {

    public static final String CODE = "SO";
    public static final String PO = "PO";

    private LocalDateTime position;
    private int period;
    private boolean zeroEnded;

    @Override
    public String getCode() {
        return CODE;
    }

}
