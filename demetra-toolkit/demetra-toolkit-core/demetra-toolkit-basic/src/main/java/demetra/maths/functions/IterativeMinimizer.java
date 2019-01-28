/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.functions;

/**
 *
 * @author palatej
 */
public class IterativeMinimizer {

    private int maxiter = 50;
    private final IFunctionMinimizer min;
    private IFunctionPoint cur;
    private IFunction fn;

    public IterativeMinimizer(final IFunctionMinimizer min) {
        this.min = min;
    }

    public boolean minimize(IFunctionPoint point) {
        cur=point;
        fn=cur.getFunction();
        IParametersDomain domain = cur.getFunction().getDomain();
        int niter = Math.max(2, domain.getDim() / 2);
        for (int i = 0; i < niter; ++i) {
            if (iterate()) {
                return true;
            }
        }
        return false;
    }

    private boolean iterate() {
        IFunctionMinimizer exemplar = min.exemplar();
        exemplar.setMaxIter(maxiter);
        boolean ok=exemplar.minimize(cur);
        IFunctionPoint ncur = exemplar.getResult();
        return true;
    }

}
