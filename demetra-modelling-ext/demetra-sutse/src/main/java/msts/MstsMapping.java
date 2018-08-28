/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msts;

import demetra.data.DataBlock;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.ssf.SsfException;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author palatej
 */
public class MstsMapping implements IParametricMapping<MultivariateCompositeSsf> {

    private final List<IMstsBuilder> builders = new ArrayList<>();
    private final List<IMstsParametersBlock> parameters = new ArrayList<>();
    private boolean scalable = true;

    public void add(IMstsBuilder decoder) {
        this.builders.add(decoder);
    }

    public void add(IMstsParametersBlock block) {
        this.parameters.add(block);
    }

    public boolean fixMaxVariance() {
        if (!scalable) {
            return false;
        }
        // TODO
        return false;
    }

    public boolean fixSmallVariance(DoubleSequence cur, double eps) {
        int pos = 0;
        double vmin = 0;
        VarianceParameter pmin = null;
        for (IMstsParametersBlock p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                if (dim == 1 && p instanceof VarianceParameter) {
                    double e = cur.get(pos);
                    double v = e * e;
                    if (v < eps && (v < vmin || pmin == null)) {
                        vmin = v;
                        pmin = (VarianceParameter) p;
                    }
                }
                pos += dim;
            }
        }
        if (pmin != null) {
            pmin.fix(0);
            return true;
        }
        return false;
    }

    private double[] array(DoubleSequence inparams) {
        double[] buffer = new double[fullDim()];
        int pos = 0;
        DoubleReader reader = inparams.reader();
        for (IMstsParametersBlock p : parameters) {
            pos = p.decode(reader, buffer, pos);
        }
        return buffer;
    }

    private double[] farray(DoubleSequence inparams) {
        double[] buffer = new double[getDim()];
        int pos = 0;
        DoubleReader reader = inparams.reader();
        for (IMstsParametersBlock p : parameters) {
            pos = p.encode(reader, buffer, pos);
        }
        return buffer;
    }

    /**
     * From function parameters to model parameters
     *
     * @param input
     * @return
     */
    public DoubleSequence trueParameters(DoubleSequence input) {
        return DoubleSequence.ofInternal(array(input));
    }

    /**
     * From model parameters to function parameters
     *
     * @param input
     * @return
     */
    public DoubleSequence functionParameters(DoubleSequence input) {
        return DoubleSequence.ofInternal(farray(input));
    }

    public int fullDim() {
        int n = 0;
        for (IMstsParametersBlock p : parameters) {
            n += p.getDomain().getDim();
        }
        return n;
    }

    @Override
    public MultivariateCompositeSsf map(DoubleSequence p) {
        MultivariateCompositeSsf.Builder builder = MultivariateCompositeSsf.builder();
        int pos = 0;
        DoubleSequence fp = trueParameters(p);
        for (IMstsBuilder decoder : builders) {
            pos = decoder.decode(fp, pos, builder);
        }
        return builder.build();
    }

    @Override
    public DoubleSequence getDefaultParameters() {
        double[] buffer = new double[getDim()];
        int pos = 0;
        for (IMstsParametersBlock p : parameters) {
            pos = p.fillDefault(buffer, pos);
        }
        return DoubleSequence.ofInternal(buffer);
    }

    @Override
    public boolean checkBoundaries(DoubleSequence inparams) {
        int pos = 0;
        for (IMstsParametersBlock p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                if (!p.getDomain().checkBoundaries(inparams.extract(pos, dim))) {
                    return false;
                }
                pos += dim;
            }
        }
        return true;
    }

    @Override
    public double epsilon(DoubleSequence inparams, int idx) {
        int pos = 0;
        for (IMstsParametersBlock p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                if (idx < pos + dim) {
                    return p.getDomain().epsilon(inparams.extract(pos, dim), idx - pos);
                }
                pos += dim;
            }
        }
        throw new SsfException(SsfException.MODEL);
    }

    @Override
    public int getDim() {
        int n = 0;
        for (IMstsParametersBlock p : parameters) {
            if (!p.isFixed()) {
                n += p.getDomain().getDim();
            }
        }
        return n;
    }

    @Override
    public double lbound(int idx) {
        int pos = 0;
        for (IMstsParametersBlock p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                if (idx < pos + dim) {
                    return p.getDomain().lbound(idx - pos);
                }
                pos += dim;
            }
        }
        throw new SsfException(SsfException.MODEL);
    }

    @Override
    public double ubound(int idx) {
        int pos = 0;
        for (IMstsParametersBlock p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                if (idx < pos + dim) {
                    return p.getDomain().ubound(idx - pos);
                }
                pos += dim;
            }
        }
        throw new SsfException(SsfException.MODEL);
    }

    @Override
    public ParamValidation validate(DataBlock ioparams) {
        boolean changed = false, invalid = false;
        int pos = 0;
        for (IMstsParametersBlock p : parameters) {
            if (!p.isFixed()) {
                int dim = p.getDomain().getDim();
                switch (p.getDomain().validate(ioparams.extract(pos, dim))) {
                    case Changed:
                        changed = true;
                        break;
                    case Invalid:
                        invalid = true;
                        break;
                }
            }
        }
        if (invalid) {
            return ParamValidation.Invalid;
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    /**
     * @return the scalable
     */
    public boolean isScalable() {
        return scalable;
    }

    /**
     * @param scalable the scalable to set
     */
    public void setScalable(boolean scalable) {
        this.scalable = scalable;
    }

}
