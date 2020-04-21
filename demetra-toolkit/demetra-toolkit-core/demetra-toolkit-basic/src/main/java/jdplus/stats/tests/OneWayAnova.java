/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.stats.tests;

import demetra.data.DoubleSeq;
import demetra.design.BuilderPattern;
import demetra.design.Development;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdplus.dstats.F;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Alpha)
@BuilderPattern(StatisticalTest.class)
public class OneWayAnova {

    private double M, SSQ, SSM, SSR;
    private int N, DFM, DFR;

    private final List<Group> groups = new ArrayList<>();

    public void add(Group group) {
        groups.add(group);
    }

    @lombok.Value
    public static final class Group {

        DoubleSeq data;
        double sx;

        public double mean() {
            return sx / data.length();
        }

        public int size() {
            return data.length();
        }

        public Group(final String name, final DoubleSeq data) {
            this.data = data;
            sx = data.sum();
        }
    }
    
    public List<Group> groups(){
        return Collections.unmodifiableList(groups);
    }

    public StatisticalTest build() {

        DFM=groups.size()-1;
        if (DFM < 1) {
            return null;
        }

        double Sx = 0;
        N = 0;
        for (Group g : groups) {
            Sx += g.sx;
            N += g.size();
        }
        M = Sx / getN();

        // compute total SSQ
        SSQ = 0;
        SSM = 0;
        for (Group g : groups) {
            SSQ += g.data.ssqc(M);
            double dm = g.mean() - M;
            SSM += dm * dm * g.size();
        }

        SSR = SSQ - SSM;
        DFR=N-groups.size();
        if (SSR < 0) {
            SSR = 0;
        }
	F f = new F(DFM, DFR);
        return new StatisticalTest(f, (SSM / DFM) * (DFR/ SSR), TestType.Upper, true);
    }

    /**
     * @return the M
     */
    public double getM() {
        return M;
    }

    /**
     * @return the SSQ
     */
    public double getSSQ() {
        return SSQ;
    }

    /**
     * @return the SSM
     */
    public double getSSM() {
        return SSM;
    }

    /**
     * @return the SSR
     */
    public double getSSR() {
        return SSR;
    }

    /**
     * @return the N
     */
    public int getN() {
        return N;
    }

    /**
     * @return the DFM
     */
    public int getDFM() {
        return DFM;
    }

    /**
     * @return the DFR
     */
    public int getDFR() {
        return DFR;
    }
    
    public double getR2(){
        return SSM/SSQ;
    }
    
    public double mean(){
        return M;
    }
    
    public double rmse(){
        return Math.sqrt(SSR/DFR);
    }
    
    public double cv(){
        return rmse()/M;
    }

}
