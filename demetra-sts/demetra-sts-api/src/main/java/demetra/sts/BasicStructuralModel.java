/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.sts;

import demetra.sa.SeriesDecomposition;

/**
 *
 * @author PALATEJ
 */
public interface BasicStructuralModel {
    
    BsmDescription getDescription();
    BsmEstimation getEstimation();
    BsmDecomposition getBsmDecomposition();
    SeriesDecomposition getFinalDecomposition();
 }
