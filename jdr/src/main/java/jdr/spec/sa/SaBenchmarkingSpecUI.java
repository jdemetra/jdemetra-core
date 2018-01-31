/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.sa;

import ec.satoolkit.benchmarking.SaBenchmarkingSpec;

/**
 *
 * @author Jean Palate
 */
public class SaBenchmarkingSpecUI{

    private final SaBenchmarkingSpec core;

    public SaBenchmarkingSpecUI(SaBenchmarkingSpec spec) {
        core = spec;
    }
    
    public boolean isEnabled()
    {
        return core.isEnabled(); 
    }
    
    public void setEnabled(boolean value){
        core.setEnabled(value);
    }
    
    public String getTarget(){
        return core.getTarget().name();
    }
    
    public void setTarget(String target){
        core.setTarget(SaBenchmarkingSpec.Target.valueOf(target));
    }
    
    public double getRho(){
        return core.getRho();
    }
    
    public void setRho(double value){
        core.setRho(value);
    }

    public double getLambda(){
        return core.getLambda();
    }
    
    public void setLambda(double value){
        core.setLambda(value);
    }
    
    public boolean isUseForecast(){
        return core.isUsingForecast();
    }

    public void setUseForecast(boolean bf){
        core.useForecast(bf);
    }

}