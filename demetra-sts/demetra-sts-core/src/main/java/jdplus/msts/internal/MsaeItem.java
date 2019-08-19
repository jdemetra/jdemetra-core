/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.StateItem;
import jdplus.msts.ArInterpreter;
import jdplus.msts.MstsMapping;
import jdplus.msts.survey.WaveSpecificSurveyErrors;
import jdplus.ssf.StateComponent;
import java.util.Arrays;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.implementations.Loading;

/**
 *
 * @author palatej
 */
public class MsaeItem extends StateItem {

    private final int nwaves;
    private final int lag;
    private final int[] lar;
    private final ArInterpreter[] par;

    public MsaeItem(String name, int nwaves, Matrix ar, boolean fixedar, int lag) {
        super(name);
        this.nwaves = nwaves;
        this.lag = lag;
        final int nar = ar.getColumnsCount();
        lar = new int[nar];
        par = new ArInterpreter[nar];
        for (int i = 0; i < nar; ++i) {
            int j = 0;
            for (; j <= i && j < ar.getRowsCount(); ++j) {
                double c = ar.get(j, i);
                if (Double.isNaN(c)) {
                    break;
                }
            }
            lar[i] = j;
            double[] car = ar.column(i).extract(0, j).toArray();
            par[i] = new ArInterpreter(name + ".wae" + (i + 1), car, fixedar);
        }
    }

    @Override
    public void addTo(MstsMapping mapping) {
        for (int i = 0; i < par.length; ++i) {
            mapping.add(par[i]);
        }
        mapping.add((p, builder) -> {
            double[][] w = new double[nwaves][];
            w[0] = DoubleSeq.EMPTYARRAY;
            int pos = 0;
            int nar = lar.length;
            for (int i = 0; i < nar; ++i) {
                w[i + 1] = p.extract(pos, lar[i]).toArray();
                pos += lar[i];
            }
            // same coefficients for the last waves, if any
            for (int i = nar + 1; i < nwaves; ++i) {
                w[i] = w[i - 1];
            }
            StateComponent cmp = WaveSpecificSurveyErrors.of(w, lag);
            builder.add(name, cmp, null);
            return pos;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(par);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        double[][] w = new double[nwaves][];
        w[0] = DoubleSeq.EMPTYARRAY;
        int pos = 0;
        int nar = lar.length;
        for (int i = 0; i < nar; ++i) {
            w[i + 1] = p.extract(pos, lar[i]).toArray();
            pos += lar[i];
        }
        // same coefficients for the last waves, if any
        for (int i = nar + 1; i < nwaves; ++i) {
            w[i] = w[i - 1];
        }
        return WaveSpecificSurveyErrors.of(w, lag);
    }

    @Override
    public int parametersCount() {
        int n = 0;
        int nar = lar.length;
        for (int i = 0; i < nar; ++i) {
            n += lar[i];
        }
        return n;
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        if (m > nwaves) {
            return null;
        } else {
            return Loading.fromPosition(2 * m);
        }
    }

    @Override
    public int defaultLoadingCount() {
        return nwaves;
    }

    @Override
    public int stateDim() {
        return 2*nwaves;
    }
}
