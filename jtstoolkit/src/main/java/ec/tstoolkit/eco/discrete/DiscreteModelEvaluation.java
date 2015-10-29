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

package ec.tstoolkit.eco.discrete;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;

/**
 *
 * @author Jean Palate
 */
public class DiscreteModelEvaluation implements IFunctionInstance {

    private DataBlock m_c;
    private double m_f = Double.NaN;
    private Matrix m_hessian;
    private DiscreteModel m_model;

    public DiscreteModelEvaluation(DiscreteModel model, IReadDataBlock p) {
        m_model = model;
        m_c = new DataBlock(p);
    }

    public DiscreteModel getModel() {
        return m_model;
    }

    public double[] gradient() {
        double[] g = m_model.loglikelihoodGradient(m_c);
        for (int i = 0; i < g.length; ++i) {
            g[i] = -g[i];
        }
        return g;
    }

    public Matrix hessian() {
        Matrix m=m_model.logLikelihoodHessian(m_c);
        m.chs();
        return m;
    }

    public double[] probabilities() {
        return m_model.probabilities(m_c);
    }

    @Override
    public IReadDataBlock getParameters() {
        return m_c;
    }

    @Override
    public double getValue() {
        return calc();
    }

    private double calc() {
        if (Double.isNaN(m_f)) {
            m_f = -m_model.loglikelihood(m_c);
        }
        return m_f;
    }
}
