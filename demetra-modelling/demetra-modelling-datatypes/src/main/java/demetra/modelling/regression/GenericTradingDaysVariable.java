/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDaysDefinition;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor
public class GenericTradingDaysVariable implements ITradingDaysVariable{
    private DayClustering dayClustering;
    private boolean contrast;
    private boolean normalized;
    
    public GenericTradingDaysVariable(GenericTradingDaysDefinition td){
        this.dayClustering=td.getClustering();
        this.contrast=td.isContrast();
        this.normalized=td.isNormalized();                
    }
    
    @Override
    public int dim(){
        int n=dayClustering.getGroupsCount();
        return contrast ? n-1 : n;
    }
}
