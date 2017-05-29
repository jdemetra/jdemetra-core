/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearmodel;

import demetra.design.Immutable;
import lombok.NonNull;
import demetra.leastsquares.LeastSquaresSolver;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Immutable
public class Ols {
    
    private final LinearModel model;
    private final LeastSquaresSolver solver;
    
    public Ols(LinearModel model){
        this.model=model;
        solver=LeastSquaresSolver.robustSolver();
    }
    
    public Ols(LinearModel model, @NonNull final LeastSquaresSolver solver){
        this.model=model;
        this. solver=solver;
    }
    
    public LinearModel getModel(){
        return model;
    }
}
