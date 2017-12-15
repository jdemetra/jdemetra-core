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
package demetra.dfm;


///**
// *
// * @author Jean Palate
// */
//public class DfmEstimator implements IDfmEstimator {
//
//    private static final String SIMPLIFIED = "Optimizing simplified model",
//            MSTEP = "Optimizing measurements", VSTEP = "Optimizing Var model", ALL = "Optimizing all parameters";
//    private int maxiter_ = 500;
//    private boolean converged_;
//    private boolean vunits_;
//    private final IFunctionMinimizer min_;
//    private int nstart_ = 15, nnext_ = 5;
//    private TsDomain idom_;
//    private boolean useBlockIterations_ = true, mixed_ = true;
//    private Likelihood ll_;
//    private DataBlock factors_;
//
//    public DfmEstimator() {
//        min_ = new ProxyMinimizer(new LevenbergMarquardtMethod());
//    }
//
//    public DfmEstimator(IFunctionMinimizer min) {
//        min_ = min.exemplar();
//    }
//
//    public TsDomain getEstimationDomain() {
//        return idom_;
//    }
//
//    public void setEstimationDomain(TsDomain dom) {
//        idom_ = dom;
//    }
//
//    public int getMaxIter() {
//        return maxiter_;
//    }
//
//    public int getMaxInitialIter() {
//        return nstart_;
//    }
//
//    public void setMaxInitialIter(int n) {
//        nstart_ = n;
//    }
//    
//    public void setPrecision(double eps){
//        min_.setConvergenceCriterion(eps);
//    }
//    
//    public double getPrecision(){
//        return min_.getConvergenceCriterion();
//    }
//
//    public int getMaxIntermediateIter() {
//        return nnext_;
//    }
//
//    public void setMaxIter(int iter) {
//        maxiter_ = iter;
//    }
//
//    public void setMaxIntermediateIter(int n) {
//        nnext_ = n;
//    }
//
//    public boolean isUsingBlockIterations() {
//        return this.useBlockIterations_;
//    }
//
//    public void setUsingBlockIterations(boolean block) {
//        this.useBlockIterations_ = block;
//    }
//
//    public boolean isMixedMethod() {
//        return mixed_;
//    }
//
//    public void setMixedMethod(boolean b) {
//        mixed_ = b;
//    }
//
//    public boolean isIndependentVarShocks() {
//        return this.vunits_;
//    }
//
//    public void setIndependentVarShocks(boolean iv) {
//        this.vunits_ = iv;
//    }
//
//    public boolean hasConverged() {
//        return converged_;
//    }
//
//    private void setMessage(String msg) {
//
//        if (min_ instanceof IProcessingHookProvider) {
//            ((IProcessingHookProvider) min_).setHookMessage(msg);
//        } else if (min_ instanceof ProxyMinimizer) {
//            ISsqFunctionMinimizer core = ((ProxyMinimizer) min_).getCore();
//            if (core instanceof IProcessingHookProvider) {
//                ((IProcessingHookProvider) core).setHookMessage(msg);
//            }
//        }
//    }
//
//    private void normalize(DynamicFactorModel model) {
//        if (vunits_) {
//            model.lnormalize();
//        } else {
//            model.normalize();
//        }
//    }
//    
//    private IDfmMapping mapping(DynamicFactorModel model, boolean mf, boolean vf) {
//        if (vunits_)
//            return new DfmMapping2(model, mf, vf);
//        else
//            return new DfmMapping(model, mf, vf);
//    }
//
//    @Override
//    public boolean estimate(final DynamicFactorModel dfm, TsInformationSet input) {
//        converged_ = false;
//        Matrix m = input.generateMatrix(idom_);
//        MSsfAlgorithm algorithm = new MSsfAlgorithm();
//        IMSsfData mdata = new MultivariateSsfData(m.subMatrix().transpose(), null);
//        MSsfFunction fn;
//        IDfmMapping mapping;
//        MSsfFunctionInstance pt;
//        int niter = 0;
//        DynamicFactorModel model = dfm.clone();
//        normalize(model);
//        try {
//            if (nstart_ > 0) {
//                setMessage(SIMPLIFIED);
//                min_.setMaxIter(nstart_);
//                SimpleDfmMapping smapping = new SimpleDfmMapping(model);
//                smapping.validate(model);
//                fn = new MSsfFunction(mdata, smapping, algorithm);
//                min_.minimize(fn, fn.evaluate(smapping.map(model)));
//                pt = (MSsfFunctionInstance) min_.getResult();
//                double var = pt.getLikelihood().getSigma();
//                DynamicFactorModel nmodel = ((DynamicFactorModel.Ssf) pt.ssf).getModel();
//                if (nmodel.isValid()) {
//                    nmodel.rescaleVariances(var);
//                    model = nmodel;
//                }
//            }
//            if (useBlockIterations_) {
//                min_.setMaxIter(nnext_);
//                while (true) {
//                    normalize(model);
//                    mapping =mapping(model, true, false);
//                    fn = new MSsfFunction(mdata, mapping, algorithm);
//                    setMessage(VSTEP);
//                    min_.minimize(fn, fn.evaluate(mapping.map(model)));
//                    niter += min_.getIterCount();
//                    pt = (MSsfFunctionInstance) min_.getResult();
//                    DynamicFactorModel nmodel = ((DynamicFactorModel.Ssf) pt.ssf).getModel();
//                    double var = pt.getLikelihood().getSigma();
//                    model = nmodel;
//                    model.rescaleVariances(var);
//                    normalize(model);
//                    if (mixed_) {
//                        DfmEM2 em = new DfmEM2(null);
//                        em.setEstimateVar(false);
//                        em.setMaxIter(nnext_);
//                        em.initialize(model, input);
//                    } else {
//                        mapping = mapping(model, false, true);
//                        fn = new MSsfFunction(mdata, mapping, algorithm);
//                        setMessage(MSTEP);
//                        min_.minimize(fn, fn.evaluate(mapping.map(model)));
//                        niter += min_.getIterCount();
//                        pt = (MSsfFunctionInstance) min_.getResult();
//                        nmodel = ((DynamicFactorModel.Ssf) pt.ssf).getModel();
//                        var = pt.getLikelihood().getSigma();
//                        model = nmodel;
//                        model.rescaleVariances(var);
//                        normalize(model);
//
//                    }
//                    mapping = mapping(model, false, false);
//                    fn = new MSsfFunction(mdata, mapping, algorithm);
//                    setMessage(ALL);
//                    converged_ = min_.minimize(fn, fn.evaluate(mapping.map(model)))
//                            && min_.getIterCount() < nnext_;
//                    niter += min_.getIterCount();
//                    pt = (MSsfFunctionInstance) min_.getResult();
//                    nmodel = ((DynamicFactorModel.Ssf) pt.ssf).getModel();
//                    var = pt.getLikelihood().getSigma();
//                    boolean stop = ll_ != null && Math.abs(ll_.getLogLikelihood() - pt.getLikelihood().getLogLikelihood()) < 1e-9;
//                    ll_ = pt.getLikelihood();
//                    model = nmodel.clone();
//                    model.rescaleVariances(var);
//                    if (converged_ || niter >= maxiter_ || stop) {
//                        break;
//                    }
//                }
//            } else {
//                normalize(model);
//                mapping =mapping(model, false, false);
//                fn = new MSsfFunction(mdata, mapping, algorithm);
//                min_.setMaxIter(maxiter_);
//                setMessage(ALL);
//                converged_ = min_.minimize(fn, fn.evaluate(mapping.map(model)));
//                pt = (MSsfFunctionInstance) min_.getResult();
//                double var = pt.getLikelihood().getSigma();
//                model = ((DynamicFactorModel.Ssf) pt.ssf).getModel().clone();
//                model.rescaleVariances(var);
//                ll_ = pt.getLikelihood();
//            }
//            return true;
//        } catch (Exception err) {
//            return false;
//        } finally {
//            normalize(model);
//            dfm.copy(model);
//            IDfmMapping fmapping = mapping(model, false, false);
//            IReadDataBlock mp = fmapping.parameters();
//            IReadDataBlock up = min_.getResult().getParameters();
//            factors_ = new DataBlock(mp.getLength());
//            for (int i = 0; i < factors_.getLength(); ++i) {
//                factors_.set(i, mp.get(i) / up.get(i));
//            }
//        }
//    }
//
//    @Override
//    public Matrix getHessian() {
//        Matrix h = min_.getCurvature();
//        if (h != null && !isLogLikelihood()) {
//            // we have to correct the hessian 
//            int ndf = ll_.getN() - h.getRowsCount();
//            h = h.times(.5 * ndf / min_.getObjective());
//        }
//        DataBlockIterator rows = h.rows(), cols = h.columns();
//        DataBlock row = rows.getData(), col = cols.getData();
//        do {
//            row.mul(factors_);
//        } while (rows.next());
//        do {
//            col.mul(factors_);
//        } while (cols.next());
//        return h;
//    }
//
//    @Override
//    public DataBlock getGradient() {
//        DataBlock grad = new DataBlock(min_.getGradient());
//        // the 
//        if (!isLogLikelihood()) {
//            // we have to correct the gradient 
//            int ndf = ll_.getN() - grad.getLength();
//            grad.mul(-.5 * ndf / min_.getObjective());
//        } else {
//            grad.chs();
//        }
//        grad.mul(factors_);
//        return grad;
//    }
//
//    public double getObjective() {
//        return min_.getObjective();
//    }
//
//    public Likelihood geLikelihood() {
//        return ll_;
//    }
//
//    public boolean isLogLikelihood() {
//        MSsfAlgorithm algorithm = new MSsfAlgorithm();
//        if (min_ instanceof ProxyMinimizer) {
//            return false;
//        } else {
//            return !algorithm.isUsingSsq();
//        }
//    }
//}
