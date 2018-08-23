/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.information;

import ec.tstoolkit.ParameterType;
import java.util.Formatter;

/**
 *
 * @author palatej
 */
public class ParameterInfo {
    public final double value, stde, pvalue;
    public final ParameterType type;
    public final String description;
    
    /**
     *
     * @param p
     * @return 
     */
    public static ParameterInfo of(ec.tstoolkit.Parameter p){
        if (p == null)
            return null;
        else
            return new ParameterInfo(p, Double.NaN, null);
    }
    /**
     * 
     * @param p
     * @param pvalue
     * @param desc
     */
    public ParameterInfo(ec.tstoolkit.Parameter p, double pvalue, String desc)
    {
        value=p.getValue();
        stde=p.getValue();
        this.pvalue=pvalue;
        type=p.getType();
        this.description=desc;
    }

    @Override
    public String toString() {
	return new Formatter().format("%g4", value).toString();
    }
    
}
