/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.filters;

import demetra.data.DoubleSeq;
import jdplus.maths.linearfilters.FiniteFilter;

/**
 *
 * @author Jean Palate
 */
public interface TrendFilterFactory {
    IFiltering createFilter();
    
}
