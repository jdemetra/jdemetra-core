/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import demetra.timeseries.TsData;
import demetra.timeseries.TsMoniker;

/**
 *
 * @author PALATEJ
 */
public class dynamicTsVariable implements ITsVariable {

    private final TsMoniker moniker;
    private final TsData data;

    protected dynamicTsVariable(final TsMoniker moniker, final TsData data) {
        this.moniker = moniker;
        this.data = data;
    }

    public TsMoniker getId() {
        return moniker;
    }

    public TsData getData() {
        return data;
    }

    protected boolean equals(dynamicTsVariable obj) {
        return moniker.equals(obj.moniker);
    }

    protected int hash() {
        return moniker.hashCode();
    }

    @Override
    public int dim() {
        return 1;
    }

}
