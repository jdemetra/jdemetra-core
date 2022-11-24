/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class TsLag implements ModifiedTsVariable.Modifier {

    int lag;

    public TsLag(int lag) {
        this.lag = lag;
    }

    @Override
    public int dim() {
        return 1;
    }

     @Override
    public String description() {
        if (lag == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        if (lag < 0) {
            builder.append('+');
        }
        builder.append(-lag);
        return builder.append(']').toString();
        
    }

}
