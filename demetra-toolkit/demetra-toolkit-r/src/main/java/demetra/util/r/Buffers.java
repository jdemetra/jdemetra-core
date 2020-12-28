/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.util.r;

import demetra.data.ParameterType;
import demetra.timeseries.TimeSelector;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Buffers {

    public int outlier(String type) {
        switch (type) {
            case "AO":
            case "ao":
                return 1;
            case "LS":
            case "ls":
                return 2;
            case "TC":
            case "tc":
                return 3;
            case "SO":
            case "so":
                return 4;
            default:
                return 0;
        }
    }

    public String outlier(int type) {
        switch (type) {
            case 1:
                return "AO";
            case 2:
                return "LS";
            case 3:
                return "TC";
            case 4:
                return "SO";
            default:
                return null;
        }
    }

    public TimeSelector selector(double[] buffer, int pos) {
        int stype = (int) buffer[pos++];
        switch (stype) {
            case 1:
                return TimeSelector.all();
            case 2:
                return TimeSelector.from(dateTime(buffer, pos));
            case 3:
                return TimeSelector.to(dateTime(buffer, pos + 3));
            case 4:
                return TimeSelector.between(dateTime(buffer, pos), dateTime(buffer, pos + 3));
            case 5:
                return TimeSelector.last((int) buffer[pos + 3]);
            case 6:
                return TimeSelector.first((int) buffer[pos]);
            case 7:
                return TimeSelector.excluding((int) buffer[pos], (int) buffer[pos + 3]);
            default:
                return TimeSelector.none();
        }
    }

    public static void selector(double[] input, int pos, TimeSelector span) {
        switch (span.getType()) {
            case All:
                input[pos++]=1;
                break;
            case From:
                input[pos++]=2;
                date(input, pos, span.getD0());
                break;
            case To:
                input[pos++]=3;
                date(input, pos+3, span.getD1());
                break;
            case Between:
                input[pos++]=4;
                date(input, pos, span.getD0());
                date(input, pos+3, span.getD1());
                break;
            case Last:
                input[pos++]=5;
                input[pos+3]=span.getN1();
                break;
            case First:
                input[pos++]=6;
                input[pos]=span.getN0();
                break;
            case Excluding:
                input[pos++]=7;
                input[pos]=span.getN0();
                input[pos+3]=span.getN1();
        }

    }

    public TimeSelector.SelectionType selectorType(int type) {
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

    public LocalDate date(double[] buffer, int pos) {
        return LocalDate.of((int) buffer[pos], (int) buffer[pos + 1], (int) buffer[pos + 2]);
    }

    public void date(double[] buffer, int pos, LocalDate date) {
        buffer[pos] = date.getYear();
        buffer[pos + 1] = date.getMonthValue();
        buffer[pos + 2] = date.getDayOfMonth();
    }

    public LocalDateTime dateTime(double[] buffer, int pos) {
        return date(buffer, pos).atStartOfDay();
    }

    public void date(double[] buffer, int pos, LocalDateTime date) {
        date(buffer, pos, date.toLocalDate());
    }

    public int parameterType(ParameterType type) {
        switch (type) {
            case Undefined:
                return 1;
            case Initial:
                return 2;
            case Fixed:
                return 3;
            case Estimated:
                return 4;
            default:
                return 0;
        }
    }

    public ParameterType parameterType(int type) {
        switch (type) {
            case 1:
                return ParameterType.Undefined;
            case 2:
                return ParameterType.Initial;
            case 3:
                return ParameterType.Fixed;
            case 4:
                return ParameterType.Estimated;
            default:
                return null;
        }
    }

}
