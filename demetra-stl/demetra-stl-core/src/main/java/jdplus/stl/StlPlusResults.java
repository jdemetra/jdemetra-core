/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.stl;

import demetra.data.DoubleSeq;
import demetra.information.GenericExplorable;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class StlPlusResults implements GenericExplorable{
    DoubleSeq trend;
    @lombok.Singular
    List<DoubleSeq> seasons;
    DoubleSeq irregular;
    DoubleSeq fit;
}
