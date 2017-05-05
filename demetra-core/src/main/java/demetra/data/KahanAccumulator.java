/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.data;

/**
 * Kahan algorithm
 * @author Jean Palate <jean.palate@nbb.be>
 */
public strictfp final class KahanAccumulator implements DoubleAccumulator{
    
    private double del, sum;
    
    @Override
    public void reset(){
        del=0;
        sum=0;
    }
    
    @Override
    public void add(double t){
        double tc=t-del; // del is the last accumulated error 
        double nsum=sum+tc; // new sum  (we add the opposite of the last accumulated error (which should dsappear)
        del=(nsum-sum)-tc; // the new accumulated error
        sum=nsum;
    }
    
    @Override
    public double sum(){
        return sum;
    }
    
    @Override
    public void set(double value){
        del=0;
        sum=value;
    }
    
    
}
