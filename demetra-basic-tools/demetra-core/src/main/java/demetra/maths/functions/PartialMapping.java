/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.maths.functions;

import demetra.data.DataBlock;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;

/**
 *
 * @author palatej
 */
public class PartialMapping<S> implements IParametricMapping<S> {

    private final IParametricMapping<S> mapping;
    private double[] refp;
    private boolean[] fp;
    private int nfixed;

    public PartialMapping(final IParametricMapping<S> mapping, DoubleSequence p, DoubleSequence pfixed) {
        this.mapping = mapping;
        this.refp = pfixed.toArray();
        this.fp = new boolean[refp.length];
        int n = 0;
        DoubleReader reader = p.reader();
        for (int i = 0; i < refp.length; ++i) {
            if (this.refp[i] != reader.next()) {
                fp[i] = true;
                ++n;
            }
        }
        nfixed = n;
    }

    private double[] narray(DoubleSequence p) {
        double[] np = refp.clone();
        DoubleReader reader = p.reader();
        for (int i = 0; i < np.length; ++i) {
            if (!fp[i]) {
                np[i] = reader.next();
            }
        }
        return np;
    }

    /**
     * Converts the partial set of parameters to the full set of parameters
     * @param p
     * @return 
     */
    public DoubleSequence convert(DoubleSequence p) {
        return DoubleSequence.ofInternal(narray(p));
    }

    private int pos(int idx) {
        for (int i = 0, j = 0; i < fp.length; ++i) {
            if (!fp[i]) {
                if (idx == j) {
                    return i;
                } else {
                    ++j;
                }
            }
        }
        return -1; // should never append
    }

    @Override
    public S map(DoubleSequence p) {
        return mapping.map(convert(p));
    }

    @Override
    public DoubleSequence getDefaultParameters() {
        return convert(mapping.getDefaultParameters());
    }

    @Override
    public boolean checkBoundaries(DoubleSequence inparams) {
        return mapping.checkBoundaries(convert(inparams));
    }

    @Override
    public double epsilon(DoubleSequence inparams, int idx) {
        return mapping.epsilon(convert(inparams), pos(idx));
    }

    @Override
    public int getDim() {
        return mapping.getDim() - nfixed;
    }

    @Override
    public double lbound(int idx) {
        return mapping.lbound(pos(idx));
    }

    @Override
    public double ubound(int idx) {
        return mapping.ubound(pos(idx));
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        double[] narray = narray(ioparams);
        DataBlock a=DataBlock.ofInternal(narray);
        ParamValidation rslt = mapping.validate(a);
        if (rslt == ParamValidation.Changed){
            int nj=narray.length-nfixed;
            for (int i=0, j=0; j<nj; ++i){
                if (!fp[i]){
                    ioparams.set(j++, narray[i]);
                }
            }
        }
        return rslt;
    }

}
