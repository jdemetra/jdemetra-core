/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x13.r;

import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.x11.MsrTable;
import demetra.x11.SeasonalFilterOption;
import demetra.x11.X11Results;
import java.util.Arrays;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
public class X11ResultsBuffer {

    public static final int START = 0, N = START + 3, MODE = N + 1, SEAS_FILTER = MODE + 1, HEND_FILTER = SEAS_FILTER + 12,
            IC_RATIO = SEAS_FILTER + 1, D9_DEF = IC_RATIO + 1, D9_FILTER = D9_DEF + 1,
            D9_PTR_MSR = D9_FILTER + 1, X11_PTR_TABLES = D9_PTR_MSR + 1,
            X11_INFO_SIZE = 32, X11_TABLES_SIZE = 38;

    private final double[] buffer;

    public X11ResultsBuffer(X11Results x11) {
        TsData b1 = x11.getB1();
        int period = b1.getAnnualFrequency();
        TsPeriod start = b1.getStart();
        int y0 = start.year(), p0 = start.annualPosition();
        int n = b1.length();
        int msrsize = msrSize(period);
        buffer = new double[X11_INFO_SIZE + msrsize + n * X11_TABLES_SIZE];

        // header
        buffer[START] = period;
        buffer[START + 1] = y0;
        buffer[START + 2] = p0;
        buffer[N] = n;
        buffer[MODE] = X11Buffer.decompositionMode(x11.getMode());
        SeasonalFilterOption[] fs = x11.getFinalSeasonalFilter();
        if (fs != null) {
            for (int i = 0; i < fs.length; ++i) {
                buffer[SEAS_FILTER + i] = X11Buffer.filter(fs[i]);
            }
        }
        buffer[HEND_FILTER] = x11.getFinalHendersonFilterLength();
        buffer[IC_RATIO] = x11.getICRatio();
        buffer[D9_DEF] = x11.isD9default() ? 1 : 0;
        buffer[D9_FILTER] = X11Buffer.filter(x11.getD9filter());
        buffer[D9_PTR_MSR] = X11_INFO_SIZE;
        buffer[X11_PTR_TABLES] = X11_INFO_SIZE + msrsize;

        fillMsr(X11_INFO_SIZE, period, x11.getD9Msr());
        fillTables(X11_INFO_SIZE + msrsize, x11);

    }

    public double[] data() {
        return buffer;
    }

    private int msrSize(int period) {
        return 1 + period * 3;
    }

    private void fillMsr(int start, int period, MsrTable msr) {
        int j = start;
        buffer[j++] = msr.getGlobalMsr();
        for (int i = 0; i < period; ++i, ++j) {
            buffer[j] = msr.getMsr(i);
        }
        double[] ri = msr.getMeanIrregularEvolutions();
        System.arraycopy(ri, 0, buffer, j, period);
        j += period;
        double[] rs = msr.getMeanSeasonalEvolutions();
        System.arraycopy(rs, 0, buffer, j, period);
    }

    private void fillTables(int i0, X11Results source) {

        TsData b1 = source.getB1();
        TsPeriod start = b1.getStart();
        int nr = b1.length(), nc = X11_TABLES_SIZE;

        int size = nr * nc, cur = i0;
        Arrays.fill(buffer, i0, i0 + size, Double.NaN);
        b1.getValues().copyTo(buffer, cur);
        cur += nr;
        TsData s = source.getB2();
        int n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB3();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB4();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB5();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB6();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB7();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB8();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB9();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB10();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB11();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB13();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB17();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getB20();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC1();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC2();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC4();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC5();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC6();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC7();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC9();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC10();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC11();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC13();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC17();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getC20();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD1();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD2();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD4();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD5();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD6();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD7();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD8();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD9();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD10();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD11();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD12();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
        cur += nr;
        s = source.getD13();
        n0 = start.until(s.getStart());
        s.getValues().copyTo(buffer, cur + n0);
    }

}
