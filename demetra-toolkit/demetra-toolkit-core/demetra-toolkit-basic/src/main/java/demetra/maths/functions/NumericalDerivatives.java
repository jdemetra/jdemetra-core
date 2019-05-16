/*
 * Copyright 2013 National Bank ofFunction Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions ofFunction the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy ofFunction the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.maths.functions;

import jdplus.data.DataBlock;
import demetra.design.Development;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class NumericalDerivatives implements IFunctionDerivatives {

    private static final int NTHREADS = Runtime.getRuntime().availableProcessors();

    private double[] eps, fp, fm, grad;
    private FastMatrix hessian;
    
    private final IFunction fn;
    private final DoubleSeq x;

    private double fx;

    private static final int NSTEPS = 2;

    /**
     *
     * @param point
     * @param sym
     */
    public NumericalDerivatives(IFunctionPoint point, boolean sym) {
        this(point, sym, false);
    }

    public NumericalDerivatives(IFunctionPoint point,
            boolean sym, boolean mt) {
        fn = point.getFunction();
        fx = point.getValue();
        x = point.getParameters();
        int n = x.length();
        fp = new double[n];
        eps = new double[n];
        if (!mt || n < 2) {
            if (!sym) {
                for (int i = 0; i < n; ++i) {
                    eps[i] = fn.getDomain().epsilon(x, i);
                    checkepsilon(i);
                    fp[i] = newval(i, eps[i]);
                }
            } else {
                fm = new double[n];
                for (int i = 0; i < n; ++i) {
                    eps[i] = fn.getDomain().epsilon(x, i);
                    checkepsilon(i);
                    fp[i] = newval(i, eps[i]);
                    fm[i] = newval(i, -eps[i]);
                }
            }
        } else {
            if (!sym) {
                for (int i = 0; i < n; ++i) {
                    eps[i] = fn.getDomain().epsilon(x, i);
                    checkepsilon(i);
                }
            } else {
                fm = new double[n];
                for (int i = 0; i < n; ++i) {
                    eps[i] = fn.getDomain().epsilon(x, i);
                    checkepsilon(i);
                }
            }
            List<Callable<Void>> tasks = createTasks(n, sym);
            ExecutorService executorService = Executors.newFixedThreadPool(NTHREADS);
            try {
                executorService.invokeAll(tasks);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            executorService.shutdown();
        }
    }

    private void calcgrad() {
        int n = fn.getDomain().getDim();
        grad = new double[n];
        for (int i = 0; i < n; ++i) {
            if (fp[i] == fx) {
                grad[i] = 0;
            } else if (fm == null) {
                grad[i] = (fp[i] - fx) / eps[i];
            } else {
                grad[i] = (fp[i] - fm[i]) / (2 * eps[i]);
            }
        }
    }

    private void calch() {
        int n = fn.getDomain().getDim();
        double[] e = new double[n];
        for (int i = 0; i < n; ++i) {
            e[i] = Math.sqrt(Math.abs(eps[i]));
         }
        hessian = CanonicalMatrix.square(n);
        // compute the diagonal
        // df/di(x) = (f(x+ei)-f(x))/ei
        // d2f/di2(x) = (df/di(x)-df/di(x-ei))/ei=(f(x+ei)-f(x)-f(x)+f(x-ei))/ei*ei
        DataBlock diag = hessian.diagonal();
        for (int i = 0; i < n; ++i) {
            double di = e[i];
            double num = newval(i, di) - 2 * fx + newval(i, -di);
            if (num != 0 && eps[i] != 0) {
                diag.set(i, num / (di*di));
            }
        }
        // other elements. 
        // df/di(x) = (f(x+ei)-f(x-ei))/(2ei)
        // d2f/didj(x) = (f(x+ei+ej)-f(x+ei-ej)-f(x-ei+ej)+f(x-ei-ej))/(4*ei*ej)
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < i; ++j) {
                double di = e[i], dj = e[j];
                double num = newval(i, j, di/2, dj/2) + newval(i, j, -di/2, -dj/2)
                        - newval(i, j, di/2, -dj/2) - newval(i, j, -di/2, dj/2);
                if (num != 0 && di != 0 && dj != 0) {
                    double z=num / (di * dj);
                    hessian.set(i, j, z);
                    hessian.set(j, i, z);
                }
            }
        }
    }

    private void checkepsilon(int i) {
        double eps = this.eps[i];
        if (eps == 0) {
            return;
        }
        DataBlock pcur=DataBlock.of(x);
        double pi = pcur.get(i);
        pcur.add(i, eps);
        if (fn.getDomain().checkBoundaries(pcur)) {
            return;
        }
        int k = 0;
        do {
            eps /= 2;
            pcur.set(i, pi + eps);
        } while (++k <= NSTEPS && !fn.getDomain().checkBoundaries(pcur));
        if (k <= NSTEPS) {
            this.eps[i] = eps;
            return;
        }
        eps = -this.eps[i];
        pcur.set(i, pi + eps);
        if (fn.getDomain().checkBoundaries(pcur)) {
            this.eps[i] = eps;
            return;
        }
        k = 0;
        do {
            eps /= 2;
            pcur.set(i, pi + eps);
        } while (++k <= NSTEPS && !fn.getDomain().checkBoundaries(pcur));
        if (k <= NSTEPS) {
            this.eps[i] = eps;
            return;
        }
        this.eps[i] = 0;
    }

    @Override
    public IFunction getFunction(){
        return fn;
    }
    
    @Override
    public DoubleSeq gradient() {
        if (grad == null) {
            calcgrad();
        }
        return DoubleSeq.of(grad);
    }

    /**
     *
     * @param h
     */
    @Override
    public void hessian(FastMatrix h) {
        if (hessian == null) {
            calch();
        }
        h.copy(hessian);
    }

    private double newval(int i, double dx) {
        try {
            DataBlock cur = DataBlock.of(x);
            cur.add(i, dx);
            IFunctionPoint fn = this.fn.evaluate(cur);
            return fn.getValue();
        } catch (Exception err) {
            return fx;
        }
    }

    private double newval(int i, int j, double dxi, double dxj) {
        try {
            DataBlock cur = DataBlock.of(x);
            cur.add(i, dxi);
            cur.add(j, dxj);
            IFunctionPoint fn = this.fn.evaluate(cur);
            return fn.getValue();
        } catch (Exception err) {
            return fx;
        }
    }

    private List<Callable<Void>> createTasks(int n, boolean sym) {
        List<Callable<Void>> result = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            result.add(new NewVal(fp, i, eps[i]));
        }
        if (sym) {
            for (int i = 0; i < n; ++i) {
                result.add(new NewVal(fm, i, -eps[i]));
            }
        }
        return result;
    }

    private class NewVal implements Callable<Void> {

        double[] rslt;
        int pos;
        double eps;

        private NewVal(double[] rslt, int pos, double eps) {
            this.rslt = rslt;
            this.pos = pos;
            this.eps = eps;

        }

        @Override
        public Void call() throws Exception {
            try {
                DataBlock cur = DataBlock.of(x);
                cur.add(pos, eps);
                IFunctionPoint fn = NumericalDerivatives.this.fn.evaluate(cur);
                rslt[pos] = fn.getValue();
            } catch (Exception err) {
                rslt[pos] = fx;
            }
            return null;
        }

    }
}
