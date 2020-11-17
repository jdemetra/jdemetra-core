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
public class TsLags implements ModifiedTsVariable.Modifier{
    
    int firstLag, lastLag;
     
    public TsLags(int firstlag, int lastlag){
        if (lastlag<firstlag)
            throw new IllegalArgumentException();
        this.firstLag=firstlag;
        this.lastLag=lastlag;
    }

 
    @Override
    public int redim(int d) {
       return getLagsCount() * d;
    }
    
     /**
     * 
     * @return
     */
    public int getLagsCount() {
        return lastLag - firstLag + 1;
    }

 }
