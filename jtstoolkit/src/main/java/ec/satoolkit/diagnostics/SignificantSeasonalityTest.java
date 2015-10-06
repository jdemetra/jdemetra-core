/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
/*
 */
package ec.satoolkit.diagnostics;

import ec.satoolkit.GenericSaProcessingFactory;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.dstats.Normal;
import ec.tstoolkit.dstats.ProbabilityType;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 * Computes the number of seasonal factors significantly different from 0 for a
 * given year.
 *
 * @author Jean Palate
 */
public class SignificantSeasonalityTest {

    public static enum Position {

        Start, Middle, End
    }

    private final Position pos;
    private final double limit;
    
    public static int[] test(IProcResults rslts, double sig){
        String str=InformationSet.concatenate(GenericSaProcessingFactory.DECOMPOSITION,ModellingDictionary.S_LIN);
        TsData s=rslts.getData(str, TsData.class);
        TsData es=rslts.getData(str+SeriesInfo.E_SUFFIX, TsData.class);
        TsData fs=rslts.getData(str+SeriesInfo.F_SUFFIX, TsData.class);
        TsData fes=rslts.getData(str+SeriesInfo.EF_SUFFIX, TsData.class);
        int[] rslt=new int[3];
        if (s != null && es != null){
            SignificantSeasonalityTest thist=new SignificantSeasonalityTest(Position.Middle, sig);
            rslt[0]=thist.significantSeasonalFactors(s, es);
            SignificantSeasonalityTest tend=new SignificantSeasonalityTest(Position.End, sig);
            rslt[1]=tend.significantSeasonalFactors(s, es);
        }else{
            rslt[0]=rslt[1]=-1;
        }
        if (fs != null && fes != null){
            SignificantSeasonalityTest tfcast=new SignificantSeasonalityTest(Position.Start, sig);
            rslt[2]=tfcast.significantSeasonalFactors(fs, fes);
            
        }else{
            rslt[2]=-1;
        }
        return rslt;
    }

    public SignificantSeasonalityTest() {
        this.pos=Position.End;
        Normal N = new Normal();
        limit = N.getProbabilityInverse(.01 / 2, ProbabilityType.Upper);
    }

    public SignificantSeasonalityTest(Position pos, double eps) {
        this.pos=pos;
        Normal N = new Normal();
        limit = N.getProbabilityInverse(eps / 2, ProbabilityType.Upper);
    }

    public int significantSeasonalFactors(TsData s, TsData es) {
        int i0 = start(s.getDomain());
        if (i0 < 0) {
            return -1;
        }
        int i1 = i0 + s.getFrequency().intValue();
        int ns = 0;
        for (int i = i0; i < i1; ++i) {
            if (Math.abs(s.get(i)) / es.get(i) > limit) {
                ++ns;
            }
        }
        return ns;
    }

    public boolean[] significantSeasons(TsData s, TsData es) {
        int i0 = start(s.getDomain());
        if (i0 < 0) {
            return null;
        }
        int f=s.getFrequency().intValue();
        boolean[] ss=new boolean[f];
        for (int j=0; j<f; ++j) {
            ss[j]=(Math.abs(s.get(i0+j)) / es.get(i0+j) > limit);
        }
        return ss;
    }

    private int start(TsDomain s) {
        int n = s.getLength();
        int f = s.getFrequency().intValue();
        if (n < f) {
            return -1;
        }
        switch (pos) {
            case Start:
                return 0;
            case Middle:
                return (n - f) / 2;
            default:
                return n - f;

        }
    }
}
