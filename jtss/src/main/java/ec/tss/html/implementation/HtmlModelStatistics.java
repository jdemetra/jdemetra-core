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
public class HtmlModelStatistics extends AbstractHtmlElement{
    
    private final ModelStatistics stats_;
    
    public HtmlModelStatistics(ModelStatistics stats){
        stats_=stats;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write("bic=").write(df4.format(stats_.bic)).newLine();
        stream.write("ser=").write(stats_.se).newLine();
        stream.write("number of outliers=").write(stats_.outliers).newLine();
        stream.write("Q (P-value)=").write(df3.format(stats_.ljungBoxPvalue)).newLine();
        stream.write("Qs (P-value)=").write(df3.format(stats_.seasLjungBoxPvalue)).newLine();
        stream.write("Skewness (P-value)=").write(df3.format(stats_.skewnessPvalue)).newLine();
    }
    
}
