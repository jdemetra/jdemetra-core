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
public class TsLags implements ModifiedTsVariable.Modifier {

    int firstLag, lastLag;

    public TsLags(int firstlag, int lastlag) {
        if (lastlag < firstlag) {
            throw new IllegalArgumentException();
        }
        this.firstLag = firstlag;
        this.lastLag = lastlag;
    }

    @Override
    public int dim() {
        return getLagsCount();
    }

    /**
     *
     * @return
     */
    public int getLagsCount() {
        return lastLag - firstLag + 1;
    }

    @Override
    public String description() {
        if (firstLag == 0 && lastLag == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        if (firstLag < 0) {
            builder.append('+');
        }
        builder.append(-firstLag);
        if (lastLag != firstLag) {
            builder.append(':');
            if (lastLag < 0) {
                builder.append('+');
            }
            builder.append(-lastLag);
        }
        return builder.append(']').toString();
    }

    @Override
    public String description(int idx) {
        int lag = firstLag + idx;
        if (lag == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        if (lag < 0) {
            builder.append('+');
        }
        builder.append(-lag).append(']');
        return builder.toString();

    }

}
