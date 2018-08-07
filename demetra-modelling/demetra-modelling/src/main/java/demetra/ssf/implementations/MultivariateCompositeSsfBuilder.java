/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.implementations;

import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.ISsfLoading;
import demetra.ssf.SsfComponent;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.ISsfErrors;
import demetra.ssf.multivariate.MultivariateSsf;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfError;
import demetra.ssf.univariate.Ssf;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author palatej
 */
public class MultivariateCompositeSsfBuilder {

    private final List<SsfComponent> components = new ArrayList<>();
    private final List<String> names = new ArrayList<>();
    private ISsfErrors measurementsError;

    private int[] dim, pos;

    public MultivariateCompositeSsfBuilder add(String name, SsfComponent cmp) {
        components.add(cmp);
        names.add(name);
        return this;
    }

    public MultivariateCompositeSsfBuilder measurementError(ISsfErrors measurementsError) {
        this.measurementsError = measurementsError;
        return this;
    }

    public IMultivariateSsf build() {
        if (components.isEmpty()) {
            return null;
        }
        // build dim / pos
        int n = components.size();
        dim = new int[n];
        pos = new int[n];
        ISsfInitialization[] i = new ISsfInitialization[n];
        ISsfDynamics[] d = new ISsfDynamics[n];
        ISsfLoading[] l = new ISsfLoading[n];
        int cpos = 0;
        for (int j = 0; j < n; ++j) {
            SsfComponent cur = components.get(j);
            pos[j] = cpos;
            dim[j] = i[j].getStateDim();
            cpos+=dim[j];
            i[j] = cur.initialization();
            d[j] = cur.dynamics();
            l[j] = cur.loading();
        }
        return new MultivariateSsf(new CompositeInitialization(dim, i),
                new CompositeDynamics(dim, d),
                new Measurements(l, measurementsError));
    }

    public int[] getComponentsDimension() {
        return dim;
    }

    public int[] getComponentsPosition() {
        return pos;
    }
}
