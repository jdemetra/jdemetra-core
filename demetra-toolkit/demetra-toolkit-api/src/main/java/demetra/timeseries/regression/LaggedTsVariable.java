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
public class LaggedTsVariable implements IModifier{
    
    private final int firstlag, lastlag;
    private final ITsVariable var;
    
    public LaggedTsVariable(ITsVariable var, int firstlag, int lastlag){
        if (lastlag<firstlag)
            throw new IllegalArgumentException();
        this.var=var;
        this.firstlag=firstlag;
        this.lastlag=lastlag;
    }

    @Override
    public ITsVariable variable() {
        return var;
     }

    @Override
    public int dim() {
       return getLagsCount() * var.dim();
    }
    
     /**
     * 
     * @return
     */
    public int getFirstLag() {
        return firstlag;
    }

    /**
     * 
     * @return
     */
    public int getLagsCount() {
        return lastlag - firstlag + 1;
    }

    /**
     * 
     * @return
     */
    public int getLastLag() {
        return lastlag;
    }

    public ITsVariable getVariable() {
        return var;
    }

}
