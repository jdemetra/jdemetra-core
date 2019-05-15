/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.implementations;

import jd.data.DataBlock;
import demetra.ssf.CompositeInitialization;
import demetra.ssf.CompositeDynamics;
import demetra.design.BuilderPattern;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.ISsfLoading;
import demetra.ssf.SsfComponent;
import demetra.ssf.StateComponent;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfError;
import demetra.ssf.univariate.Measurement;
import demetra.ssf.univariate.Ssf;
import java.util.ArrayList;
import java.util.List;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.util.IntList;

/**
 *
 * @author palatej
 */
public class CompositeSsf extends Ssf {

    private final int[] pos;
    private final int[] dim;

    private CompositeSsf(int[] cmpPos, int[] cmpDim, ISsfInitialization init, ISsfDynamics dynamics, ISsfMeasurement measurement) {
        super(init, dynamics, measurement);
        this.pos = cmpPos;
        this.dim = cmpDim;
    }

    public int[] componentsPosition() {
        return pos.clone();
    }

    public int[] componentsDimension() {
        return dim.clone();
    }

    @BuilderPattern(CompositeSsf.class)
    public static class Builder {

        private final List<SsfComponent> components = new ArrayList<>();
        private ISsfError measurementError;

        private int[] dim, pos;

        public Builder add(SsfComponent cmp) {
            components.add(cmp);
            return this;
        }

        public Builder add(StateComponent cmp, ISsfLoading loading) {
            components.add(new SsfComponent(cmp.initialization(), cmp.dynamics(), loading));
            return this;
        }

        public Builder measurementError(ISsfError measurementError) {
            this.measurementError = measurementError;
            return this;
        }

        public Builder measurementError(double var) {
            this.measurementError = MeasurementError.of(var);
            return this;
        }

        public CompositeSsf build() {
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
                i[j] = cur.initialization();
                d[j] = cur.dynamics();
                l[j] = cur.loading();
                pos[j] = cpos;
                dim[j] = i[j].getStateDim();
                cpos += dim[j];
            }
            // optimization
            ISsfLoading cl = Loading.optimize(new CompositeLoading(dim, l), cpos);

            return new CompositeSsf(pos, dim, new CompositeInitialization(dim, i),
                    new CompositeDynamics(dim, d),
                    new Measurement(cl, measurementError));
        }

        public int[] getComponentsDimension() {
            return dim;
        }

        public int[] getComponentsPosition() {
            return pos;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
