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
public class EmSpec implements Cloneable {

    public static final int DEF_VERSION = 2, DEF_MAXITER = 100, DEF_MAXNUMITER = 50;
    public static final double DEF_PRECISION = 1e-9;
    public static final String ENABLED = "enabled", VERSION = "version", MAXITER = "maxiter", MAXNUMITER = "maxnumiter", PRECISION = "precision";

    private boolean enabled_;
    private int version_ = DEF_VERSION;
    private int maxIter_ = DEF_MAXITER;
     private int maxNumIter_ = DEF_MAXNUMITER;
    private double eps_ = DEF_PRECISION;

    public void setEnabled(boolean use) {
        enabled_ = use;
    }

    public boolean isEnabled() {
        return enabled_;
    }

    public void setMaxIter(int iter) {
        maxIter_ = iter;
    }

    public int getMaxIter() {
        return maxIter_;
    }

    public void setMaxNumIter(int iter) {
        maxNumIter_ = iter;
    }

    public int getMaxNumIter() {
        return maxNumIter_;
    }

    public void setVersion(int v) {
        version_ = v;
    }

    public int getVersion() {
        return version_;
    }

    public void setPrecision(double e) {
        eps_ = e;
    }

    public double getPrecision() {
        return eps_;
    }

    @Override
    public EmSpec clone() {
        try {
            return (EmSpec) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof EmSpec && equals((EmSpec) obj));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.enabled_ ? 1 : 0);
        hash = 41 * hash + this.version_;
        hash = 41 * hash + this.maxIter_;
        return hash;
    }

    public boolean equals(EmSpec spec) {
        return enabled_ == spec.enabled_ && maxIter_ == spec.maxIter_
                && maxNumIter_ == spec.maxNumIter_ && version_ == spec.version_
                && spec.eps_ == eps_;
    }

}
