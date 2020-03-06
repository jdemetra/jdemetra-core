/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x11;

import demetra.data.DoubleSeq;
import demetra.sa.DecompositionMode;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.x11.X11;
import demetra.x11.X11Exception;
import demetra.x11.X11Results;
import demetra.x11.X11Spec;
import jdplus.x11.filter.MusgraveFilterFactory;
import jdplus.x11.filter.endpoints.AsymmetricEndPoints;
import jdplus.x11.pseudoadd.X11BStepPseudoAdd;
import jdplus.x11.pseudoadd.X11CStepPseudoAdd;
import jdplus.x11.pseudoadd.X11DStepPseudoAdd;
import java.util.Arrays;
import jdplus.data.DataBlock;
import jdplus.math.linearfilters.FiniteFilter;
import jdplus.math.linearfilters.SymmetricFilter;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Getter
public class X11Kernel implements X11.Processor {

    private X11BStep bstep;
    private X11CStep cstep;
    private X11DStep dstep;
    private TsData input;
    private X11Context context;
    private DoubleSeq kernelD13;

    public static double[] table(int n, double value) {
        double[] x = new double[n];
        Arrays.fill(x, value);
        return x;
    }

    @Override
    public X11Results process(@lombok.NonNull TsData timeSeries, @lombok.NonNull X11Spec spec) {
        clear();
        check(timeSeries, spec);

        input = timeSeries;
        DoubleSeq data = input.getValues();
        context = X11Context.of(spec, input);

        if (context.isPseudoAdd()) {
            bstep = new X11BStepPseudoAdd();
            bstep.process(data, context);
            cstep = new X11CStepPseudoAdd(bstep.getB7(), bstep.getB13());
            cstep.process(data, bstep.getB20(), context);
            dstep = new X11DStepPseudoAdd(cstep.getC7(), cstep.getC13(), cstep.getC20());
            dstep.process(data, cstep.getC20(), context);
        } else {
            if (context.isLogAdd()) {
                data = data.log();
            }
            bstep = new X11BStep();
            bstep.process(data, context);
            cstep = new X11CStep();
            cstep.process(data, bstep.getB20(), context);
            dstep = new X11DStep();
            dstep.process(data, cstep.getC20(), context);
        }
        return buildResults(timeSeries.getStart(), spec);
    }

    private void check(TsData timeSeries, X11Spec spec) throws X11Exception, IllegalArgumentException {
        int frequency = timeSeries.getAnnualFrequency();
        if (frequency == -1) {
            throw new IllegalArgumentException("Frequency of the time series must be compatible with years");
        }
        if (timeSeries.getValues().length() < 3 * frequency) {
            throw new X11Exception(X11Exception.ERR_LENGTH);
        }
        if (!timeSeries.getValues().allMatch(Double::isFinite)) {
            throw new X11Exception(X11Exception.ERR_MISSING);
        }
        if ((spec.getMode() == DecompositionMode.Multiplicative || spec.getMode() == DecompositionMode.LogAdditive)
                && timeSeries.getValues().anyMatch(x -> x <= 0)) {
            throw new X11Exception(X11Exception.ERR_NEG);
        }
    }

    private void clear() {
        bstep = null;
        cstep = null;
        dstep = null;
        input = null;
        context = null;
    }

    private X11Results buildResults(TsPeriod start, X11Spec spec) {
        return X11Results.builder()
                //B-Tables
                .b1(input)
                .b2(TsData.ofInternal(start.plus(bstep.getB2drop()), prepare(bstep.getB2())))
                .b3(TsData.ofInternal(start.plus(bstep.getB2drop()), prepare(bstep.getB3())))
                .b4(TsData.ofInternal(start.plus(bstep.getB2drop()), prepare(bstep.getB4())))
                .b5(TsData.ofInternal(start, prepare(bstep.getB5())))
                .b6(TsData.ofInternal(start, prepare(bstep.getB6())))
                .b7(TsData.ofInternal(start, prepare(bstep.getB7())))
                .b8(TsData.ofInternal(start, prepare(bstep.getB8())))
                .b9(TsData.ofInternal(start, prepare(bstep.getB9())))
                .b10(TsData.ofInternal(start, prepare(bstep.getB10())))
                .b11(TsData.ofInternal(start, prepare(bstep.getB11())))
                .b13(TsData.ofInternal(start, prepare(bstep.getB13())))
                .b17(TsData.ofInternal(start, bstep.getB17()))
                .b20(TsData.ofInternal(start, prepare(bstep.getB20())))
                //C-Tables
                .c1(TsData.ofInternal(start, prepare(cstep.getC1())))
                .c2(TsData.ofInternal(start.plus(cstep.getC2drop()), prepare(cstep.getC2())))
                .c4(TsData.ofInternal(start.plus(cstep.getC2drop()), prepare(cstep.getC4())))
                .c5(TsData.ofInternal(start, prepare(cstep.getC5())))
                .c6(TsData.ofInternal(start, prepare(cstep.getC6())))
                .c7(TsData.ofInternal(start, prepare(cstep.getC7())))
                .c9(TsData.ofInternal(start, prepare(cstep.getC9())))
                .c10(TsData.ofInternal(start, prepare(cstep.getC10())))
                .c11(TsData.ofInternal(start, prepare(cstep.getC11())))
                .c13(TsData.ofInternal(start, prepare(cstep.getC13())))
                .c17(TsData.ofInternal(start, cstep.getC17()))
                .c20(TsData.ofInternal(start, prepare(cstep.getC20())))
                //D-Tables
                .d1(TsData.ofInternal(start, prepare(dstep.getD1())))
                .d2(TsData.ofInternal(start.plus(dstep.getD2drop()), prepare(dstep.getD2())))
                .d4(TsData.ofInternal(start.plus(dstep.getD2drop()), prepare(dstep.getD4())))
                .d5(TsData.ofInternal(start, prepare(dstep.getD5())))
                .d6(TsData.ofInternal(start, prepare(dstep.getD6())))
                .d7(TsData.ofInternal(start, prepare(dstep.getD7())))
                .d8(TsData.ofInternal(start, prepare(dstep.getD8())))
                .d9(TsData.ofInternal(start, prepare(dstep.getD9())))
                .d10(TsData.ofInternal(start, prepare(dstep.getD10())))
                .d11(TsData.ofInternal(start, prepare(dstep.getD11())))
                .d12(TsData.ofInternal(start, prepare(dstep.getD12(), dstep.getD10(), cstep.getC13())))
                .d13(TsData.ofInternal(start, kernelD13))
                //Final
                .iCRatio(dstep.getICRatio())
                .mode(spec.getMode())
                .finalHendersonFilterLength(dstep.getFinalHendersonFilterLength())
                .build();
    }

    private DoubleSeq prepare(final DoubleSeq in) {
        return prepare(in, null, null);
    }

    private DoubleSeq prepare(final DoubleSeq t, final DoubleSeq s, final DoubleSeq i) {
        DoubleSeq dsT = t;
        if (context.isLogAdd()) {
            dsT = dsT.exp();
            if (s != null && i != null) {
                dsT = legacyBiasCorrection(dsT, s, i);
                DoubleSeq dsTpos = X11Context.makePositivity(dsT);
                DoubleSeq dsS = DoubleSeq.onMapping(s.length(), l -> bstep.getB1().exp().get(l) / s.exp().get(l));
                kernelD13 = DoubleSeq.onMapping(dsTpos.length(), k -> dsS.get(k) / dsTpos.get(k));
            }
        } else {
            kernelD13 = dstep.getD13();
        }
        return dsT;
    }

    private DoubleSeq legacyBiasCorrection(DoubleSeq t, DoubleSeq s, DoubleSeq i) {
        s = prepare(s);
        i = prepare(i);
        double issq = i.log().ssq();
        double sig = Math.exp(issq / (2 * i.length()));
        int ifreq = context.getPeriod();
        int length = (ifreq == 2) ? 5 : 2 * ifreq - 1;;

        double[] x = table(s.length(), Double.NaN);
        int ndrop = length / 2;
        DataBlock out = DataBlock.of(x, ndrop, x.length - ndrop);

        SymmetricFilter smoother = context.trendFilter(length);
        smoother.apply(s, out);
        FiniteFilter[] musgraveFilters = MusgraveFilterFactory.makeFilters(smoother, 4.5);
        AsymmetricEndPoints aepFilter = new AsymmetricEndPoints(musgraveFilters, 0);
        DataBlock dbout2 = out.extend(ndrop, ndrop);
        aepFilter.process(s, dbout2);

        DoubleSeq hs = DoubleSeq.of(dbout2.toArray());

        int n = t.length();

        return DoubleSeq.onMapping(n, j -> t.get(j) * hs.get(j) * sig);
    }

}
