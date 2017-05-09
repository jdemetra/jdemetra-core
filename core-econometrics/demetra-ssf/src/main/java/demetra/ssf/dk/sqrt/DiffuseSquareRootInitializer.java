/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.dk.sqrt;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.matrices.ElementaryTransformations;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.SsfException;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.ssf.akf.AugmentedState;
import demetra.ssf.dk.DiffuseUpdateInformation;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.univariate.OrdinaryFilter;

/**
 * Mixed algorithm based on the diffuse initializer copyOf Durbin-Koopman and on the
 (square root) array filter copyOf Kailath for the diffuse part. That solution
 provides a much more stable estimate copyOf the diffuse part.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class DiffuseSquareRootInitializer implements OrdinaryFilter.Initializer {

    public interface Transformation{
        void transform(DataBlock row, Matrix A); 
    }

    private Transformation fn=(DataBlock row,Matrix A) ->ElementaryTransformations.fastRowGivens(row, A); 
    private final IDiffuseSquareRootFilteringResults results;
    private AugmentedState astate;
    private DiffuseUpdateInformation pe;
    private ISsfMeasurement measurement;
    private ISsfDynamics dynamics;
    private ISsfData data;
    private int t, endpos;
    private DataBlock Z;

    public DiffuseSquareRootInitializer() {
        this.results = null;
    }

    /**
     *
     * @param results
     */
    public DiffuseSquareRootInitializer(IDiffuseSquareRootFilteringResults results) {
        this.results = results;
    }

    /**
     * @return the fn
     */
    public Transformation getTransformation() {
        return fn;
    }

    /**
     * @param fn the fn to set
     */
    public void setTransformation(Transformation fn) {
        this.fn = fn;
    }
    
    /**
     *
     * @param ssf
     * @param data
     * @param state
     * @return
     */
    @Override
    public int initialize(final State state, final ISsf ssf, final ISsfData data) {
        measurement = ssf.getMeasurement();
        dynamics = ssf.getDynamics();
        this.data = data;
        t = 0;
        int end = data.length();
        if (!initState()) {
            return -1;
        }
        while (t < end) {
            if (!astate.isDiffuse()) {
                break;
            }
            // astate contains a(t|t-1), P(t|t-1)
            if (results != null) {
                results.save(t, astate, StateInfo.Forecast);
            }
            if (error(t)) {
                // pe contains e(t), f(t), C(t), Ci(t)
                if (results != null) {
                    results.save(t, pe);
                }
                update();
            } else {
                if (results != null) {
                    results.save(t, pe);
                }
            }
            if (results != null) {
                results.save(t, astate, StateInfo.Concurrent);
            }
            // astate contains now a(t+1|t), P(t+1|t), B(t+1)
            astate.next(t++, dynamics);
        }

        if (results != null) {
            results.close(t);
        }
        state.P().copy(this.astate.P());
        state.a().copy(this.astate.a());
        endpos=t;
        return t;
    }
    
    public int getEndDiffusePos(){
        return endpos;
    }

    private boolean initState() {
        int r = dynamics.getStateDim();
        astate = AugmentedState.of(dynamics);
        if (astate == null) {
            return false;
        }
        pe = new DiffuseUpdateInformation(r);
        Z = DataBlock.make(astate.getDiffuseDim());
        dynamics.diffuseConstraints(constraints());
        return true;
    }


    /**
     * Computes P(t|t), a(t|t)
     */
    private void update() {
        if (pe.isDiffuse()) {
            update1();
        } else {
            update0();
        }
    }

    private void update0() {
        double f = pe.getVariance(), e = pe.get();
        DataBlock C = pe.M();
        Matrix P = astate.P();
        P.addXaXt(-1 / f, C);

        // state
        // a0 = a0 + f1*Mi*v0.
        if (data.hasData()) {
            double c = e / f;
            astate.a().addAY(c, C);
        }
    }

    private void update1() {
        double fi = pe.getDiffuseNorm2(), f = pe.getVariance(), e = pe.get();
        DataBlock C = pe.M(), Ci = pe.Mi();
        // P = T P T' - 1/f*(TMf)(TMf)'+RQR'+f*(TMf/f-TMi/fi)(TMf/f-TMi/fi)'
        astate.P().addXaXt(-1 / f, C);

        DataBlock tmp = DataBlock.copyOf(C);
        tmp.addAY(-f / fi, Ci);
        astate.P().addXaXt(1 / f, tmp);

        if (data.hasData()) {
            // a0 = a0 + f1*Mi*v0. Reuse Mf as temporary buffer
            astate.a().addAY(e / fi, Ci);
        }
    }

    /**
     * Computes  
     * e(t)=y(t)-Z(t)a(t|t-1)
     * f(t)=Z(t)P(t|t-1)Z'(t)+h(t)
     * C(t)=Z(t)P(t|t-1)
     * Ci(t) by array algorithm
     * @return true if y non missing
     */
    private boolean error(int t) {
        // calc f and fi
        // fi = Z Pi Z' , f = Z P Z' + H
        preArray();
        DataBlock z=zconstraints();
        double fi = z.ssq();
        if (fi < State.ZERO) {
            fi = 0;
        }
        pe.setDiffuseNorm2(fi);

        double f = measurement.ZVZ(t, astate.P());
        if (measurement.hasErrors()) {
            f += measurement.errorVariance(t);
        }
        if (Math.abs(f) < State.ZERO) {
            f = 0;
        }

        pe.setVariance(f);
        if (data.hasData()) {
            double y = data.get(t);
            if (Double.isNaN(y)) {
                pe.setMissing();
                return false;
            } else {
                pe.set(y - measurement.ZX(t, astate.a()));
            }
        }

        DataBlock C = pe.M();
        measurement.ZM(t, astate.P(), C);
        if (pe.isDiffuse()) {
            Matrix B = constraints();
           fn.transform(z, B);
            pe.Mi().setAY(z.get(0), B.column(0));
            // move right
            astate.dropDiffuseConstraint();
        }
        return true;
    }

    // Array routines
    //     |R Z*X|
    // X = |     | 
    //     |0   X|
    // XX' = |RR'+ZXX'Z' ZXX'| = |AA'     AB'|
    //       |XX'Z'      XX' | = |BA' BB'+CC'|
    // A = Fi^1/2
    // B = Ci * Fi^-1/2
    // C = X(t+1)
    private void preArray() {
        DataBlock zconstraints = zconstraints();
        zconstraints.set(0);
        Matrix A = constraints();
        measurement.ZM(t, A, zconstraints);
        //dynamics.TM(pos, A);
    }

    private Matrix constraints() {
        return astate.B();
    }

    private DataBlock zconstraints() {
        return Z.range(0, astate.getDiffuseDim());
    }

//    private void checkDiffuse() {
//        SubMatrix C = constraints();
//        for (int c = ndiffuse_ - 1; c >= 0; --c) {
//            if (C.column(c).nrm2() < State.ZERO) {
//                --ndiffuse_;
//            } else {
//                break;
//            }
//        }
//    }
}
