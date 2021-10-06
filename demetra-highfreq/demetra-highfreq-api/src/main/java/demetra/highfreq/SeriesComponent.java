/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq;

import demetra.data.DoubleSeq;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class SeriesComponent {
    String name;
    DoubleSeq data;
    DoubleSeq stde;
}
