/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.timeseries.calendars.DayClustering;

/**
 *
 * @author palatej
 */
@lombok.Value
public class GenericTradingDaysDef implements ITsVariableDefinition{
    private DayClustering dayClustering;
    private boolean contrast;
    private boolean normalized;
    private int period;
    
    @Override
    public int dim(){
        int n=dayClustering.getGroupsCount();
        return contrast ? n-1 : n;
    }
}
