/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.ssf.implementations;

import demetra.design.BuilderPattern;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.ISsfLoading;
import demetra.ssf.SsfComponent;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfError;
import demetra.ssf.univariate.Measurement;
import demetra.ssf.univariate.Ssf;
import java.util.ArrayList;
import java.util.List;
import demetra.ssf.univariate.ISsfMeasurement;

/**
 *
 * @author palatej
 */
public class CompositeSsf implements ISsf {

    @lombok.experimental.Delegate
    private final Ssf ssf;
    private final int[] cmpPos;

    private CompositeSsf(int[] cmpPos, ISsfInitialization init, ISsfDynamics dynamics, ISsfMeasurement measurement) {
        ssf = new Ssf(init, dynamics, measurement);
        this.cmpPos = cmpPos;
    }

    public int[] componentsPosition() {
        return cmpPos.clone();
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
            return new CompositeSsf(pos, new CompositeInitialization(dim, i),
                    new CompositeDynamics(dim, d),
                    new Measurement(new CompositeLoading(dim, l), measurementError));
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
