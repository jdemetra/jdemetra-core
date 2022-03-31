/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq;

import demetra.information.GenericExplorable;
import demetra.processing.HasLog;
import demetra.processing.ProcessingLog;
import demetra.sa.SeriesDecomposition;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class ExtendedAirlineResults implements GenericExplorable, HasLog{

    private ExtendedRegAirlineModel preprocessing;
    private ExtendedAirlineDecomposition decomposition;
    private SeriesDecomposition components, finals;
    private ProcessingLog log;
    
}
