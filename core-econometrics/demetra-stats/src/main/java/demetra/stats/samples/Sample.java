/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.stats.samples;

import demetra.data.DoubleSequence;
import java.util.stream.DoubleStream;

/**
 * Basic statistics on a sample. The items of the sample can be retrieved by a stream.
 * @author Jean Palate <jean.palate@nbb.be>
 */
public interface Sample {
    
    DoubleStream all();

    double mean();

    double variance();
    
    int size();
    
    Population population();

}
