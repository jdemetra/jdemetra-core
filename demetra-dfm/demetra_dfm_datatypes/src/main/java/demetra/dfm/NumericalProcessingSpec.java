/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.dfm;

import java.util.Map;

/**
 *
 * @author palatej
 */
public class NumericalProcessingSpec implements Cloneable {
    
    public static enum Method{
        Lbfgs,
        LevenbergMarquardt
    }

    public static final int DEF_VERSION = 2, DEF_MAXITER = 1000, DEF_MAXSITER = 15,
            DEF_NITER = 5;
    public static final Boolean DEF_BLOCK = true, DEF_MIXED=true, DEF_IVAR=false;
    public static final String ENABLED = "enabled", MAXITER = "maxiter", MAXSITER = "maxsiter", NITER = "niter", 
            BLOCKITER = "blockiter", METHOD="method", EPS = "eps", MIXED="mixed", IVAR="ivar";
    public static final double DEF_EPS = 1e-9;
    private boolean enabled_;
    private int maxiter_ = DEF_MAXITER, maxsiter_ = DEF_MAXSITER, niter_ = DEF_NITER;
    private boolean block_ = DEF_BLOCK, mixed_=DEF_MIXED, ivar_=DEF_IVAR;
    private double eps_ = DEF_EPS;
    private Method method_ = Method.LevenbergMarquardt;

    public void setEnabled(boolean use) {
        enabled_ = use;
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setMaxIter(int iter) {
        maxiter_ = iter;
    }

    public int getMaxIter() {
        return maxiter_;
    }

    public void setMaxInitialIter(int iter) {
        maxsiter_ = iter;
    }

    public int getMaxInitialIter() {
        return maxsiter_;
    }

    public void setMaxIntermediateIter(int iter) {
        niter_ = iter;
    }

    public int getMaxIntermediateIter() {
        return niter_;
    }
    
    public boolean isBlockIterations(){
        return block_;
    }
    
    public void setBlockIterations(boolean b){
        block_=b;
    }
    
    public boolean isMixedEstimation(){
        return mixed_;
    }
    
    public void setMixedEstimation(boolean b){
        mixed_=b;
    }

    public boolean isIndependentVarShocks(){
        return ivar_;
    }
    
    public void setIndependentVarShocks(boolean b){
        ivar_=b;
    }

    public Method getMethod(){
        return method_;
    }
    
    public void setMethod(Method m){
        method_=m;
    }
    
    public double getPrecision(){
        return eps_;
    }

    public void setPrecision(double eps){
        eps_=eps;
    }
    
    @Override
    public NumericalProcessingSpec clone() {
        try {
            return (NumericalProcessingSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof NumericalProcessingSpec && equals((NumericalProcessingSpec) obj));
    }

    public boolean equals(NumericalProcessingSpec obj) {
        return obj.enabled_ == enabled_ && obj.block_ == block_ && obj.mixed_ == mixed_
                && obj.ivar_== ivar_ && obj.eps_ == eps_ && obj.method_ == method_
                && obj.maxiter_ == maxiter_ && obj.maxsiter_ == obj.maxsiter_ && obj.niter_ == niter_;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.enabled_ ? 1 : 0);
        hash = 17 * hash + this.maxiter_;
        hash = 17 * hash + this.maxsiter_;
        hash = 17 * hash + this.niter_;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.eps_) ^ (Double.doubleToLongBits(this.eps_) >>> 32));
        return hash;
    }

}
