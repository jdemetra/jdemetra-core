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
package demetra.toolkit.io.protobuf;

import demetra.data.Parameter;
import demetra.data.ParameterType;
import demetra.data.Utility;
import demetra.likelihood.LikelihoodStatistics;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ToolkitProtosUtility {

    public TimeSelector convert(ToolkitProtos.TimeSelector sel) {
        switch (sel.getType()) {
            case SPAN_ALL:
                return TimeSelector.all();
            case SPAN_FROM: {
                String d0 = sel.getD0();
                if (d0 == null) {
                    throw new IllegalArgumentException("Span not correctly initialized");
                }
                LocalDate ld = LocalDate.parse(d0, DateTimeFormatter.ISO_DATE);
                return TimeSelector.from(ld.atStartOfDay());
            }
            case SPAN_TO: {
                String d1 = sel.getD1();
                if (d1 == null) {
                    throw new IllegalArgumentException("Span not correctly initialized");
                }
                LocalDate ld = LocalDate.parse(d1, DateTimeFormatter.ISO_DATE);
                return TimeSelector.to(ld.atStartOfDay());
            }
            case SPAN_BETWEEN: {
                String d0 = sel.getD0(), d1 = sel.getD1();
                if (d0 == null || d1 == null) {
                    throw new IllegalArgumentException("Span not correctly initialized");
                }
                LocalDate ld0 = LocalDate.parse(d0, DateTimeFormatter.ISO_DATE);
                LocalDate ld1 = LocalDate.parse(d1, DateTimeFormatter.ISO_DATE);
                return TimeSelector.between(ld0.atStartOfDay(), ld1.atStartOfDay());
            }
            case SPAN_FIRST: {
                int n0 = sel.getN0();
                return TimeSelector.first(n0);
            }
            case SPAN_LAST: {
                int n1 = sel.getN1();
                return TimeSelector.last(n1);
            }
            case SPAN_EXCLUDING: {
                int n0 = sel.getN0(), n1 = sel.getN1();
                return TimeSelector.excluding(n0, n1);
            }
            default:
                return TimeSelector.none();
        }
    }

    public void fill(TimeSelector sel, ToolkitProtos.TimeSelector.Builder builder) {
        switch (sel.getType()) {
            case All:
                builder.setType(ToolkitProtos.SelectionType.SPAN_ALL);
                break;
            case From:
                builder.setType(ToolkitProtos.SelectionType.SPAN_FROM)
                        .setD0(sel.getD0().toLocalDate().format(DateTimeFormatter.ISO_DATE));
                break;
            case To:
                builder.setType(ToolkitProtos.SelectionType.SPAN_TO)
                        .setD1(sel.getD1().toLocalDate().format(DateTimeFormatter.ISO_DATE));
                break;
            case Between:
                builder.setType(ToolkitProtos.SelectionType.SPAN_BETWEEN)
                        .setD0(sel.getD0().toLocalDate().format(DateTimeFormatter.ISO_DATE))
                        .setD1(sel.getD1().toLocalDate().format(DateTimeFormatter.ISO_DATE));
                break;
            case First:
                builder.setType(ToolkitProtos.SelectionType.SPAN_FIRST)
                        .setN0(sel.getN0());
                break;
            case Last:
                builder.setType(ToolkitProtos.SelectionType.SPAN_LAST)
                        .setN1(sel.getN1());
                break;
            case Excluding:
                builder.setType(ToolkitProtos.SelectionType.SPAN_EXCLUDING)
                        .setN0(sel.getN0())
                        .setN1(sel.getN1());
                break;

            default:
                builder.setType(ToolkitProtos.SelectionType.SPAN_UNSPECIFIED);
        }
    }

    public ToolkitProtos.TimeSelector convert(TimeSelector sel) {
        ToolkitProtos.TimeSelector.Builder builder = ToolkitProtos.TimeSelector.newBuilder();
        fill(sel, builder);
        return builder.build();
    }

    public ParameterType convert(ToolkitProtos.ParameterType t) {
        switch (t) {
            case PARAMETER_FIXED:
                return ParameterType.Fixed;
            case PARAMETER_INITIAL:
                return ParameterType.Initial;
            case PARAMETER_ESTIMATED:
                return ParameterType.Estimated;
            default:
                return ParameterType.Undefined;
        }
    }

    public ToolkitProtos.ParameterType convert(ParameterType t) {
        switch (t) {
            case Fixed:
                return ToolkitProtos.ParameterType.PARAMETER_FIXED;
            case Initial:
                return ToolkitProtos.ParameterType.PARAMETER_INITIAL;
            case Estimated:
                return ToolkitProtos.ParameterType.PARAMETER_ESTIMATED;
            default:
                return ToolkitProtos.ParameterType.PARAMETER_UNSPECIFIED;
        }
    }

    public Parameter convert(ToolkitProtos.Parameter p) {
        switch (p.getType()) {
            case PARAMETER_FIXED:
                return Parameter.fixed(p.getValue());
            case PARAMETER_INITIAL:
                return Parameter.initial(p.getValue());
            case PARAMETER_ESTIMATED:
                return Parameter.estimated(p.getValue());
            default:
                return Parameter.undefined();

        }
    }

    public ToolkitProtos.Parameter convert(Parameter p) {
        return ToolkitProtos.Parameter.newBuilder()
                .setType(convert(p.getType()))
                .setValue(p.getValue())
                .build();
    }

    public Parameter[] convert(List<ToolkitProtos.Parameter> p) {
        int n = p.size();
        if (n == 0) {
            return EMPTY_P;
        } else {
            Parameter[] np = new Parameter[n];
            for (int i = 0; i < n; ++i) {
                np[i] = convert(p.get(i));
            }
            return np;
        }
    }

    public TsData convert(ToolkitProtos.TsData s) {
        int p = s.getPeriod(), y = s.getStartYear(), m = s.getStartPeriod();
        int n = s.getValueCount();
        double[] data = new double[n];
        for (int i = 0; i < n; ++i) {
            data[i] = s.getValue(i);
        }
        return ts(p, y, m, data);
    }

    public ToolkitProtos.TsData convert(TsData s) {
        TsPeriod start = s.getStart();
        return ToolkitProtos.TsData.newBuilder()
                .setPeriod(s.getAnnualFrequency())
                .setStartYear(start.year())
                .setStartPeriod(start.annualPosition())
                .addAllValue(Utility.asIterable(s.getValues()))
                .build();
    }
    
    public ToolkitProtos.Matrix convert(MatrixType m){
        return ToolkitProtos.Matrix.newBuilder()
                .setNrows(m.getRowsCount())
                .setNcols(m.getColumnsCount())
                .addAllValue(Utility.asIterable(m.toArray()))
                .build();
    }
    
    public ToolkitProtos.LikelihoodStatistics convert(LikelihoodStatistics ls){
        return ToolkitProtos.LikelihoodStatistics.newBuilder()
                .setNobs(ls.getObservationsCount())
                .setNeffectiveobs(ls.getEffectiveObservationsCount())
                .setNparams(ls.getEstimatedParametersCount())
                .setDegreesOfFreedom(ls.getEffectiveObservationsCount()-ls.getEstimatedParametersCount())
                .setLogLikelihood(ls.getLogLikelihood())
                .setAdjustedLogLikelihood(ls.getAdjustedLogLikelihood())
                .setAic(ls.getAIC())
                .setAicc(ls.getAICC())
                .setBic(ls.getBIC())
                .setBicc(ls.getBICC())
                .setBic2(ls.getBIC2())
                .setHannanQuinn(ls.getHannanQuinn())
                .setSsq(ls.getSsqErr())
                .build();
                
    }

    private final Parameter[] EMPTY_P = new Parameter[0];

    TsData ts(int freq, int year, int start, double[] data) {
        switch (freq) {
            case 1:
                return TsData.ofInternal(TsPeriod.yearly(year), data);
            case 12:
                return TsData.ofInternal(TsPeriod.monthly(year, start), data);
            default:
                int c = 12 / freq;
                TsPeriod pstart = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), LocalDate.of(year, (start - 1) * c + 1, 1));
                return TsData.ofInternal(pstart, data);
        }
    }
}
