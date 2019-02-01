/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.benchmarking.univariate;

import demetra.benchmarking.univariate.DentonSpec;
import demetra.timeseries.TsException;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import demetra.timeseries.simplets.TsDataToolkit;
import org.openide.util.lookup.ServiceProvider;
import demetra.benchmarking.univariate.Denton;
import demetra.data.AggregationType;
import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.linearsystem.LinearSystemSolver;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.maths.polynomials.Polynomial;
import demetra.maths.polynomials.UnitRoots;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = Denton.Processor.class)
public class DentonProcessor implements Denton.Processor {

    @Override
    public TsData benchmark(TsData highFreqSeries, TsData aggregationConstraint, DentonSpec spec) {
        int ratio = highFreqSeries.getTsUnit().ratioOf(aggregationConstraint.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        // Y is limited to q !
        TimeSelector qsel = TimeSelector.between(highFreqSeries.getStart().start(), highFreqSeries.getPeriod(highFreqSeries.length()).start());
        TsData naggregationConstraint = TsDataToolkit.select(aggregationConstraint, qsel);
        TsPeriod sh = highFreqSeries.getStart();
        TsPeriod sl = TsPeriod.of(sh.getUnit(), naggregationConstraint.getStart().start());
        int offset = sh.until(sl);
        MatrixDenton denton = new MatrixDenton(spec, ratio, offset);
        double[] r = denton.process(highFreqSeries.getValues(), naggregationConstraint.getValues());
        return TsData.ofInternal(sh, r);
    }

    @Override
    public TsData benchmark(TsUnit highFreq, TsData aggregationConstraint, DentonSpec spec) {
        int ratio = highFreq.ratioOf(aggregationConstraint.getTsUnit());
        if (ratio == TsUnit.NO_RATIO || ratio == TsUnit.NO_STRICT_RATIO) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        // Y is limited to q !
        TsPeriod sh = TsPeriod.of(highFreq, aggregationConstraint.getStart().start());
        MatrixDenton denton = new MatrixDenton(spec, ratio, 0);
        double[] r = denton.process(aggregationConstraint.getValues());
        return TsData.ofInternal(sh, r);
    }


}
