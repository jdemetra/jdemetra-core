/*
 * Copyright 2013-2014 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.univariate;

import jdplus.data.DataBlock;
import jdplus.ssf.DataBlockResults;
import jdplus.ssf.DataResults;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.MatrixResults;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class DefaultDisturbanceSmoothingResults implements IDisturbanceSmoothingResults {

    private final DataBlockResults U;
    private final MatrixResults UVar;
    private final DataResults e, evar;

    private DefaultDisturbanceSmoothingResults(final boolean cov, final boolean err) {
        U = new DataBlockResults();
        UVar = cov ? new MatrixResults() : null;
        if (err) {
            e = new DataResults();
            if (cov) {
                evar = new DataResults();
            } else {
                evar = null;
            }
        } else {
            e = null;
            evar = null;
        }
    }

    public static DefaultDisturbanceSmoothingResults full(boolean err) {
        return new DefaultDisturbanceSmoothingResults(true, err);
    }

    public static DefaultDisturbanceSmoothingResults light(boolean err) {
        return new DefaultDisturbanceSmoothingResults(false, err);
    }

    @Override
    public void saveSmoothedTransitionDisturbances(int t, DataBlock u, FastMatrix uvar) {
        U.save(t, u);

        if (UVar != null && uvar != null) {
            UVar.save(t, uvar);
        }
    }

    @Override
    public void saveSmoothedMeasurementDisturbance(int t, double err, double v) {
        if (e == null) {
            return;
        }
        e.save(t, err);
        if (evar != null) {
            evar.save(t, v);
        }
    }

    public DataBlock uComponent(int item) {
        return U.item(item);
    }

    public DataBlock uComponentVar(int item) {
        return UVar.item(item, item);
    }

    public DataBlock e() {
        return e.all();
    }

    public DataBlock evar() {
        return evar.all();
    }

    @Override
    public DataBlock u(int pos) {
        return U.datablock(pos);
    }

    @Override
    public FastMatrix uVar(int pos) {
        return UVar == null ? null : UVar.matrix(pos);
    }

    @Override
    public double e(int pos) {
        return e == null ? 0 : e.get(pos);
    }

    @Override
    public double eVar(int pos) {
        return evar == null ? 0 : evar.get(pos);
    }

    public int getStart() {
        return U.getStartSaving();
    }

    @Override
    public void prepare(ISsf ssf, int start, int end) {
        ISsfDynamics dynamics = ssf.dynamics();
        int edim = dynamics.getInnovationsDim();
        if (e != null) {
            e.prepare(start, end);
            if (evar != null) {
                evar.prepare(start, end);
            }
        }
        U.prepare(edim, start, end);

        if (UVar != null) {
            UVar.prepare(edim, start, end);
        }
    }

    @Override
    public void rescaleVariances(double factor){
        if (evar != null)
            evar.rescale(factor);
        if (UVar != null)
            UVar.rescale(factor);
        
        // TO DO: Check the next two lines
        double se = Math.sqrt(factor); 
        U.rescale(se); 
    }
    
}
