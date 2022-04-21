/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stl;

import demetra.stl.LoessSpecification;
import java.util.function.IntToDoubleFunction;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class LoessFilter {

    private final LoessSpecification spec;

    public LoessFilter(LoessSpecification spec) {
        this.spec = spec;
    }

    public boolean filter(IDataGetter y, IntToDoubleFunction userWeights, IDataSelector ys) {
        int i0 = y.getStart(), i1 = y.getEnd(), j0 = ys.getStart(), j1 = ys.getEnd();
        if (j0 > i0 || j1 < i1 || i1 <= i0) // not supported
        {
            return false;
        }
        int n = i1 - i0;
        int win = spec.getWindow();
        if (n == 1) {
            double v = y.get(i0);
            for (int j = j0; j < j1; ++j) {
                ys.set(j, v);
            }
            return true;
        }

        int newnj = Math.min(spec.getJump(), n - 1);
        int nleft = 0, nright = 0;
        if (win >= n) {
            nleft = 0;
            nright = n - 1;
            for (int i = i0; i < i1; i += newnj) {
                double yscur = loess(y, i, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys.set(i, yscur);
                } else {
                    ys.set(i, y.get(i));
                }
            }
            // complete the backcasts, forecasts (without jumps)
            for (int i = i0 - 1; i >= j0; --i) {
                double yscur = loess(y, i, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys.set(i, yscur);
                } else {
                    ys.set(i, ys.get(i + 1));
                }
            }
            for (int i = i1; i < j1; ++i) {
                double yscur = loess(y, i, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys.set(i, yscur);
                } else {
                    ys.set(i, ys.get(i - 1));
                }
            }
        } else if (newnj == 1) {
            int nsh = (win - 1) >> 1;
            nleft = i0;
            nright = i0 + win - 1;
            for (int i = i0; i < i1; ++i) {
                if (i > nsh && nright != n - 1) {
                    ++nleft;
                    ++nright;
                }
                double yscur = loess(y, i, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys.set(i, yscur);
                } else {
                    ys.set(i, y.get(i));
                }
            }
        } else {
            int nsh = (win - 1) >> 1;
            for (int i = i0; i < i1; i += newnj) {
                if (i < nsh) {
                    nleft = i0;
                    nright = i0 + win - 1;
                } else if (i >= i1 - nsh) {
                    nright = i1 - 1;
                    nleft = i1 - win;
                } else {
                    nleft = i - nsh;
                    nright = i + nsh;
                }

                double yscur = loess(y, i, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys.set(i, yscur);
                } else {
                    ys.set(i, y.get(i));
                }
            }
        }
        if (newnj != 1) {

            int i = i0;
            for (; i < i1 - newnj; i += newnj) {
                double delta = (ys.get(i + newnj) - ys.get(i)) / newnj;
                for (int j = i + 1; j < i + newnj; ++j) {
                    ys.set(j, ys.get(i) + delta * (j - i));
                }
            }

            if (i != i1 - 1) {
                double yscur = loess(y, i1 - 1, nleft, nright, userWeights);
                if (Double.isFinite(yscur)) {
                    ys.set(i1 - 1, yscur);
                } else {
                    ys.set(i1 - 1, y.get(i1 - 1));
                }
                double delta = (ys.get(i1 - 1) - ys.get(i)) / (i1 - i - 1);
                for (int j = i + 1; j < i1 - 1; ++j) {
                    ys.set(j, ys.get(i) + delta * (j - i));
                }
            }
        }
        nleft = i0;
        nright = i0 + Math.min(win - 1, n-1);
        // complete the backcasts, forecasts (without jumps)
        for (int i = i0 - 1; i >= j0; --i) {
            double yscur = loess(y, i, nleft, nright, userWeights);
            if (Double.isFinite(yscur)) {
                ys.set(i, yscur);
            } else {
                ys.set(i, ys.get(i + 1));
            }
        }
        nright = i1 - 1;
        nleft = i1 - Math.min(win, n);
        for (int i = i1; i < j1; ++i) {
            double yscur = loess(y, i, nleft, nright, userWeights);
            if (Double.isFinite(yscur)) {
                ys.set(i, yscur);
            } else {
                ys.set(i, ys.get(i - 1));
            }
        }
        return true;
    }

    private double loess(IDataGetter y, int ix, int nleft, int nright, IntToDoubleFunction userWeights) {
        int n = y.getLength();
        int nw = nright - nleft + 1;
        double[] w = new double[nw];
        double range = n - 1;
        double h = Math.max(ix - nleft, nright - ix);
        if (spec.getWindow() > n) {
            h += (spec.getWindow() - n) * .5;
        }
        double h9 = 0.999 * h;
        double h1 = 0.001 * h;
        double a = 0;
        for (int j = nleft, jw = 0; j <= nright; ++j, ++jw) {
            boolean ok = Double.isFinite(y.get(j));
            if (ok) {
                double r = Math.abs(j - ix);
                if (r < h9) {
                    if (r < h1) {
                        w[jw] = 1;
                    } else {
                        w[jw] = spec.weights().applyAsDouble(r / h);
                    }

                    if (userWeights != null) {
                        w[jw] *= userWeights.applyAsDouble(j);
                    }
                    a += w[jw];
                }
            }
        }

        if (a <= 0) {
            return Double.NaN;
        } else {
            for (int j = 0; j < nw; ++j) {
                w[j] /= a;
            }
            if (h > 0 && spec.getDegree() > 0) {
                a = 0;
                for (int j = 0; j < nw; ++j) {
                    if (w[j] != 0) {
                        a += w[j] * j;
                    }
                }
                double b = ix - nleft - a;
                double c = 0;
                for (int j = 0; j < nw; ++j) {
                    if (w[j] != 0) {
                        double ja = j - a;
                        c += w[j] * ja * ja;
                    }
                }
                if (Math.sqrt(c) > .001 * range) {
                    b /= c;

                    for (int j = 0; j < nw; ++j) {
                        if (w[j] != 0) {
                            w[j] *= b * (j - a) + 1;
                        }
                    }
                }
            }
            double ys = 0;
            for (int j = nleft, jw = 0; j <= nright; ++j, ++jw) {
                if (w[jw] != 0) {
                    ys += w[jw] * y.get(j);
                }
            }
            return ys;
        }
    }

    /**
     * @return the spec
     */
    public LoessSpecification getSpec() {
        return spec;
    }

}
