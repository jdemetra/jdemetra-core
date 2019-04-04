/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts;

import demetra.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import demetra.data.DoubleSeq;
import demetra.maths.functions.IParametricMapping;
import demetra.maths.functions.ParamValidation;
import demetra.ssf.SsfException;
import demetra.ssf.implementations.MultivariateCompositeSsf;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author palatej
 */
public class MstsMapping implements IParametricMapping<MultivariateCompositeSsf> {

    private final List<IMstsBuilder> builders = new ArrayList<>();
    private final List<ParameterInterpreter> parameters = new ArrayList<>();
    private boolean scalable = true;

    public void add(IMstsBuilder decoder) {
        this.builders.add(decoder);
    }

    public void add(ParameterInterpreter block) {
        this.parameters.add(block);
    }

    public Stream<ParameterInterpreter> parameters() {
        return parameters.stream();
    }

    public List<VarianceInterpreter> smallVariances(DoubleSeq cur, double eps) {
        List<VarianceInterpreter> small = new ArrayList<>();
        double max = maxVariance(cur);
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            int dim = p.getDomain().getDim();
            if (!p.isFixed() && dim == 1 && p instanceof VarianceInterpreter) {
                VarianceInterpreter vp = (VarianceInterpreter) p;
                if (vp.isNullable()) {
                    double v = cur.get(pos);
                    if (v < eps * max) {
                        small.add(vp);
                    }
                }
            }

            pos += dim;
        }
        return small;
    }

    public void fixModelParameters(Predicate<ParameterInterpreter> selection, DoubleSeq fullParameters) {
        ParameterInterpreter.fixModelParameters(parameters, selection, fullParameters);
    }

    public double maxVariance(DoubleSeq cur) {
        double max = 0;
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            int dim = p.getDomain().getDim();
            if (dim == 1 && !p.isFixed() && p instanceof VarianceInterpreter) {
                double v = cur.get(pos);
                if (v > max) {
                    max = v;
                }
            }
            pos += dim;
        }
        return max;
    }

    public VarianceInterpreter fixMaxVariance(double[] pcur, double var) {
        double max = 0;
        int pos = 0;
        VarianceInterpreter mvar = null;
        for (ParameterInterpreter p : parameters) {
            int dim = p.getDomain().getDim();
            if (dim == 1 && p instanceof VarianceInterpreter) {
                double v = pcur[pos];
                if (v > max) {
                    max = v;
                    mvar = (VarianceInterpreter) p;
                }
            }
            pos += dim;
        }
        if (max > 0 && max != var) {
            rescaleVariances(var / max, pcur);
        }
        mvar.fixStde(Math.sqrt(var));
        return mvar;
    }

    public void rescaleVariances(double factor, double[] curp) {
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            pos = p.rescaleVariances(factor, curp, pos);
        }
    }

    /**
     * From function parameters to model parameters
     *
     * @param input
     * @return
     */
    public DoubleSeq modelParameters(DoubleSeq input) {
        return DoubleSeq.ofInternal(ParameterInterpreter.decode(parameters, input));
    }

    /**
     * From model parameters to function parameters
     *
     * @param input
     * @return
     */
    public DoubleSeq functionParameters(DoubleSeq input) {
        return DoubleSeq.ofInternal(ParameterInterpreter.encode(parameters, input));
    }

    @Override
    public MultivariateCompositeSsf map(DoubleSeq p) {
        MultivariateCompositeSsf.Builder builder = MultivariateCompositeSsf.builder();
        DoubleSeq fp = modelParameters(p);
        for (IMstsBuilder decoder : builders) {
            int np = decoder.decode(fp, builder);
            fp = fp.drop(np, 0);
        }
        return builder.build();
    }

    @Override
    public DoubleSeq getDefaultParameters() {
        double[] buffer = new double[getDim()];
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
            pos = p.fillDefault(buffer, pos);
        }
        return DoubleSeq.ofInternal(buffer);
    }

    @Override
    public boolean checkBoundaries(DoubleSeq inparams) {
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
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
    public double epsilon(DoubleSeq inparams, int idx) {
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
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
        return ParameterInterpreter.dim(parameters.stream());
    }

    @Override
    public double lbound(int idx) {
        int pos = 0;
        for (ParameterInterpreter p : parameters) {
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
        for (ParameterInterpreter p : parameters) {
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
        for (ParameterInterpreter p : parameters) {
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
                pos += dim;
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

    public String[] parametersName() {
        List<String> names = new ArrayList<>();
        for (ParameterInterpreter block : parameters) {
            int n = block.getDomain().getDim();
            if (n == 1) {
                names.add(block.getName());
            } else {
                for (int i = 1; i <= n; ++i) {
                    names.add(block.getName() + "_" + i);
                }
            }
        }
        return names.toArray(new String[names.size()]);
    }

}
