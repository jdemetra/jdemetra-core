/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.data.DoubleSeq;

/**
 * Description of a fractional airline model with multiple periodicities
 * @author palatej
 */
@lombok.Value
public class FractionalAirline {

    private double[] periodicities;
    private DoubleSeq theta;
    private int ndifferencing;
    
}
