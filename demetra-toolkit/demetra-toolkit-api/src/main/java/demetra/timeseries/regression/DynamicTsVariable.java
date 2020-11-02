/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.regression;

import demetra.timeseries.TsMoniker;

/**
 *
 * @author PALATEJ
 */
public class DynamicTsVariable implements IUserVariable {

    private final TsMoniker moniker;

    protected DynamicTsVariable(final TsMoniker moniker) {
        this.moniker = moniker;
    }

    public TsMoniker getId() {
        return moniker;
    }

    protected boolean equals(DynamicTsVariable obj) {
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
