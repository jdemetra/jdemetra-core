/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import jdplus.sa.StationaryVarianceDecomposition;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName="Builder")
public class TramoSeatsDiagnostics {
  
    private StationaryVarianceDecomposition varianceDecomposition;

}
