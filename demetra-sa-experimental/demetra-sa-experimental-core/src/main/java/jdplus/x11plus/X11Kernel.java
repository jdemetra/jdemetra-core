/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11plus;

import java.util.Arrays;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class X11Kernel {

//    private X11AStep astep;
    private X11BStep bstep;
    private X11CStep cstep;
    private X11DStep dstep;
    private X11EStep estep;

    static double[] table(int n, double value) {
        double[] x = new double[n];
        Arrays.fill(x, value);
        return x;
    }
    
    public void process(DoubleSeq data, X11Context context) {
        clear();
        bstep = new X11BStep();
        bstep.process(data, context);
        cstep = new X11CStep();
        cstep.process(data, context.remove(data, bstep.getB20()), context);
        dstep = new X11DStep();
        dstep.process(data, context.remove(data, cstep.getC20()), context);
        
    }

    /**
     * @return the bstep
     */
    public X11BStep getBstep() {
        return bstep;
    }

    /**
     * @return the cstep
     */
    public X11CStep getCstep() {
        return cstep;
    }

    /**
     * @return the dstep
     */
    public X11DStep getDstep() {
        return dstep;
    }

    /**
     * @return the estep
     */
    public X11EStep getEstep() {
        return estep;
    }

    private void clear() {
        bstep = null;
        cstep = null;
        dstep = null;
        estep = null;
    }
}
