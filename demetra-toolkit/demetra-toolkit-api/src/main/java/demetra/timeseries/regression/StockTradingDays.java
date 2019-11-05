/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

/**
 *
 * @author palatej
 */

@lombok.Value
public class StockTradingDays implements ITradingDaysVariable{
    /**
     * W-th day of the month. 0-based! When w is negative, the (-w) day before the end of the month is considered
     * See documentation of X12 Arima, for instance.
    */
    private int w;

    @Override
    public int dim() {
        return 6;
    }

}
