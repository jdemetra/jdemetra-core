/*
* Copyright 2013 National Bank of Belgium
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
package demetra.maths.functions.minpack;

import demetra.data.DataBlock;
import demetra.data.Doubles;
import demetra.design.Development;
import demetra.maths.functions.FunctionException;
import demetra.maths.functions.ssq.ISsqFunction;
import demetra.maths.functions.ssq.ISsqFunctionDerivatives;
import demetra.maths.functions.ssq.ISsqFunctionPoint;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class SsqEstimationProblem implements IEstimationProblem {

    private DataBlock parameters;
    private ISsqFunctionPoint ftry;
    private final ISsqFunction fn;
    private ISsqFunctionDerivatives derivatives;

    /**
     *
     * @param fn
     * @param start
     */
    public SsqEstimationProblem(ISsqFunctionPoint start) {
        ftry = start;
        fn = ftry.getSsqFunction();
        parameters = DataBlock.copyOf(start.getParameters());
    }

    @Override
    public void bound(int idx, boolean bound) {
        throw new FunctionException(
                "The method or operation is not implemented.");
    }

    private void calc() {
        fn.getDomain().validate(parameters);
        try {
            ftry = fn.ssqEvaluate(parameters);
        } catch (Exception err) {
            ftry = null;
        }
    }

    private void clear() {
        derivatives = null;
        ftry = null;
    }

    @Override
    public double getMeasurementParialDerivative(int midx, int pidx) {
        if (ftry == null) {
            calc();
        }
        if (ftry == null) {
            return Math.sqrt(Double.MAX_VALUE);
        }
        if (derivatives == null) {
            derivatives = ftry.ssqDerivatives();
        }
        return -derivatives.dEdX(pidx).get(midx);
    }

    /**
     *
     * @return
     */
    @Override
    public int getMeasurementsCount() {
        if (ftry == null) {
            calc();
        }
        return ftry == null ? 0 : ftry.getE().length();
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public double getMeasurementValue(int idx) {
        if (ftry == null) {
            calc();
        }
        return ftry == null ? Math.sqrt(Double.MAX_VALUE) : ftry.getE().get(idx);
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public double getMeasurementWheight(int idx) {
        return 1;
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public double getParameterEstimate(int idx) {
        return parameters.get(idx);
    }

    @Override
    public int getParametersCount() {
        return parameters.length();
    }

    /**
     *
     * @param midx
     * @return
     */
    @Override
    public double getResidual(int midx) {
        if (ftry == null) {
            calc();
        }
        return ftry == null ? Math.sqrt(Double.MAX_VALUE) : ftry.getE().get(midx);
    }

    /**
     *
     * @return
     */
    public ISsqFunctionPoint getResult() {
        if (ftry == null) {
            calc();
        }
        return ftry;
    }

    /**
     *
     * @param midx
     * @return
     */
    @Override
    public double getTheoreticalValue(int midx) {
        return 0;
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public double getUnboundParameterEstimate(int idx) {
        return parameters.get(idx);
    }

    /**
     *
     * @return
     */
    @Override
    public int getUnboundParametersCount() {
        return parameters.length();
    }

    /**
     *
     * @param idx
     * @param ignore
     */
    @Override
    public void ignoreMeasurement(int idx, boolean ignore) {
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public boolean isBound(int idx) {
        return false;
    }

    /**
     *
     * @param idx
     * @return
     */
    @Override
    public boolean isMeasurementIgnore(int idx) {
        return false;
    }

    /**
     *
     * @param idx
     * @param val
     */
    @Override
    public void setParameterEstimate(int idx, double val) {
        if (parameters.get(idx) != val) {
            parameters.set(idx, val);
            clear();
        }
    }

    /**
     *
     * @param idx
     * @param val
     */
    @Override
    public void setUnboundParameterEstimate(int idx, double val) {
        if (parameters.get(idx) != val) {
            parameters.set(idx, val);
            clear();
        }
    }

    public Doubles gradient() {
        if (this.ftry == null) {
            calc();
        }
        if (ftry == null) {
            return null;
        } else {
            return ftry.ssqDerivatives().gradient();
        }
    }
}
