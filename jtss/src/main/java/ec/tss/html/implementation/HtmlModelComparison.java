/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tss.html.implementation;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tstoolkit.modelling.arima.ModelStatistics;
import java.io.IOException;

/**
 *
 * @author Jean Palate
 */
public class HtmlModelComparison extends AbstractHtmlElement{
    
    private final ModelStatistics[] stats_;
    
    public HtmlModelComparison(ModelStatistics[] stats){
        stats_=stats;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
