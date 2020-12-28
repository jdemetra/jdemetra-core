/*
 * Copyright 2020 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.x13.r;

import demetra.regarima.OutlierSpec;
import demetra.regarima.SingleOutlierSpec;
import demetra.timeseries.TimeSelector;
import demetra.util.r.Buffer;
import demetra.util.r.Buffers;
import java.util.List;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class OutlierBuffer extends Buffer<OutlierSpec> {

    public static final int NTYPES = 6, TYPE = 0, VA = TYPE + NTYPES, DEF_VA = VA + NTYPES,
            SEL = DEF_VA + 1, METHOD = SEL + 7, TCRATE = METHOD + 1,
            MAXITER = TCRATE + 1, LSRUN = MAXITER + 1, SIZE = LSRUN + 1;

    public static OutlierBuffer of(OutlierSpec spec) {
        double[] input = new double[SIZE];
        List<SingleOutlierSpec> types = spec.getTypes();
        for (int i = 0; i < types.size(); ++i) {
            SingleOutlierSpec sspec = types.get(i);
            input[TYPE + i] = Buffers.outlier(sspec.getType());
            input[VA + i] = sspec.getCriticalValue();
        }
        input[DEF_VA] = spec.getDefaultCriticalValue();
        Buffers.selector(input, SEL, spec.getSpan());
        input[TCRATE] = spec.getMonthlyTCRate();
        input[METHOD] = spec.getMethod() == spec.getMethod().AddOne ? 1 : 2;
        input[MAXITER] = spec.getMaxIter();
        input[LSRUN] = spec.getLsRun();
        return new OutlierBuffer(input);
    }

    public OutlierBuffer(double[] data) {
        super(data);
    }

    public TimeSelector selector() {
        return Buffers.selector(buffer, SEL);
    }

    public static TimeSelector.SelectionType selectorType(int type) {
        switch (type) {
            case 1:
                return TimeSelector.SelectionType.All;
            case 2:
                return TimeSelector.SelectionType.From;
            case 3:
                return TimeSelector.SelectionType.To;
            case 4:
                return TimeSelector.SelectionType.Between;
            case 5:
                return TimeSelector.SelectionType.Last;
            case 6:
                return TimeSelector.SelectionType.First;
            case 7:
                return TimeSelector.SelectionType.Excluding;
            default:
                return TimeSelector.SelectionType.None;
        }
    }

    @Override
    public OutlierSpec build() {
        OutlierSpec.Builder builder = OutlierSpec.builder()
                .defaultCriticalValue(buffer[DEF_VA])
                .monthlyTCRate(buffer[TCRATE])
                .maxIter((int) buffer[MAXITER])
                .lsRun((int) buffer[LSRUN])
                .span(Buffers.selector(buffer, SEL))
                .method(buffer[METHOD] == 1 ? OutlierSpec.Method.AddOne : OutlierSpec.Method.AddAll);
        for (int i = 0; i < NTYPES; ++i) {
            int t = (int) buffer[TYPE + i];
            if (t == 0) {
                break;
            }
            builder.type(new SingleOutlierSpec(Buffers.outlier(t), buffer[VA + i]));
        }
        return builder.build();
    }

}
