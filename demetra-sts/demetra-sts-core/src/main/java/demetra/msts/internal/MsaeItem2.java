/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.data.DoubleSequence;
import demetra.maths.MatrixType;
import demetra.msts.ArInterpreter;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceInterpreter;
import demetra.msts.survey.WaveSpecificSurveyErrors2;
import demetra.ssf.StateComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import demetra.msts.ParameterInterpreter;

/**
 *
 * @author palatej
 */
public class MsaeItem2 extends AbstractModelItem {
    
    private final VarianceInterpreter[] v;
    private final int lag;
    private final int[] lar;
    private final ArInterpreter[] par;
    
    public MsaeItem2(String name, double[] v, boolean fixedVar, MatrixType ar, boolean fixedar, int lag) {
        super(name);
        int nwaves = v.length;
        this.lag = lag;
        final int nar = ar.getColumnsCount();
        lar = new int[nar];
        par = new ArInterpreter[nar];
        this.v=new VarianceInterpreter[nwaves];
        for (int i=0; i<nwaves; ++i){
            this.v[i]=new VarianceInterpreter(name + ".var" + (i+1), v[i], fixedVar, true);
        }
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
        for (int i = 0; i < v.length; ++i) {
            mapping.add(v[i]);
        }
        for (int i = 0; i < par.length; ++i) {
            mapping.add(par[i]);
        }
        mapping.add((p, builder) -> {
            int nwaves=v.length;
            double[] var=new double[nwaves];
            int pos = 0;
            for (int i=0; i<nwaves; ++i){
                var[i]=p.get(pos++);
            }
            double[][] w = new double[nwaves][];
            w[0] = DoubleSequence.EMPTYARRAY;
            int nar = lar.length;
            for (int i = 0; i < nar; ++i) {
                w[i + 1] = p.extract(pos, lar[i]).toArray();
                pos += lar[i];
            }
            // same coefficients for the last waves, if any
            for (int i = nar + 1; i < nwaves; ++i) {
                w[i] = w[i - 1];
            }
            StateComponent cmp = WaveSpecificSurveyErrors2.of(var, w, lag);
            builder.add(name, cmp, null);
            return pos;
        });
    }
    
    @Override
    public List<ParameterInterpreter> parameters() {
        List<ParameterInterpreter> all=new ArrayList<>();
        for (int i=0; i<v.length; ++i)
            all.add(v[i]);
        for (int i=0; i<par.length; ++i)
            all.add(par[i]);
        return all;
    }
    
}
