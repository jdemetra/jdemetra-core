/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.msts.internal;

import demetra.maths.matrices.Matrix;
import demetra.msts.ArInterpreter;
import demetra.msts.MstsMapping;
import demetra.msts.VarianceInterpreter;
import demetra.msts.survey.WaveSpecificSurveyErrors3;
import demetra.ssf.StateComponent;
import java.util.ArrayList;
import java.util.List;
import demetra.msts.ParameterInterpreter;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author palatej
 */
public class MsaeItem3 extends AbstractModelItem {
    
    private final VarianceInterpreter[] v;
    private final Matrix k;
    private final int lag;
    private final ArInterpreter[] par;
    
    public MsaeItem3(String name, double[] v, boolean fixedVar, double[] ar, boolean fixedar, Matrix k, int lag) {
        super(name);
        int nwaves = v.length;
        this.lag = lag;
        this.k=k;
        final int nar = ar.length;
        par = new ArInterpreter[nar];
        this.v=new VarianceInterpreter[nwaves];
        for (int i=0; i<nwaves; ++i){
            this.v[i]=new VarianceInterpreter(name + ".var" + (i+1), v[i], fixedVar, true);
        }
        for (int i = 0; i < nar; ++i) {
            par[i] = new ArInterpreter(name + ".wae" + (i + 1), new double[]{ar[i]}, fixedar);
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
            double[] ar = new double[nwaves-1];
            for (int i = 0; i < par.length; ++i) {
                ar[i] = p.get(pos++);
            }
            // same coefficients for the last waves, if any
            for (int i = par.length + 1; i < ar.length; ++i) {
                ar[i] = ar[i - 1];
            }
            StateComponent cmp = WaveSpecificSurveyErrors3.of(var, ar, k, lag);
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
