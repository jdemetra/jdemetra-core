/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.filter;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;

/**
 *
 * @author C. Hofer, N.Gonschorreck
 */
public class X11SeasonalFilterProcessor {

    private final IFiltering[] filters;

    public X11SeasonalFilterProcessor(IFiltering[] filters) {
        this.filters = filters;
    }

    /**
     *
     * @param input
     * @param start start period of the input zero based
     * @return
     */
    public DoubleSequence process(DoubleSequence input, int start) {

        double[] x = new double[input.length()];
        DataBlock out = DataBlock.ofInternal(x);
        DataBlock in = DataBlock.of(input);
        int period = filters.length;
        int index = start;
        for (int i = 0; i < period; ++i) {
            index = (start + i) % period;
            DataBlock cin = in.extract(i, -1, period);
            DataBlock cout = out.extract(i, -1, period);
            DataBlock ccout = filters[index].process(cin);
            cout.set(ccout, y -> y);

        }

        return DoubleSequence.ofInternal(x);
    }

}
