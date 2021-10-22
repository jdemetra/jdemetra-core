/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendar.r;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.Easter;
import demetra.timeseries.calendars.Holiday;
import demetra.toolkit.io.protobuf.CalendarProtosUtility;
import demetra.toolkit.io.protobuf.ToolkitProtos;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.modelling.regression.GenericTradingDaysFactory;
import jdplus.timeseries.calendars.HolidaysUtility;
import demetra.math.matrices.Matrix;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Calendars {

    public byte[] calendarToBuffer(Calendar calendar) {
        return CalendarProtosUtility.convert(calendar).toByteArray();
    }

    public Calendar calendarOf(byte[] buffer) {
        try {
            ToolkitProtos.Calendar cal = ToolkitProtos.Calendar.parseFrom(buffer);
            return CalendarProtosUtility.convert(cal);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    public String[] easter(int y0, int y1, boolean julian) {
        String[] rslt = new String[y1 - y0 + 1];
        for (int y = y0, i = 0; y <= y1; ++y, ++i) {
            LocalDate e;
            if (julian) {
                e = Easter.julianEaster(y, true);
            } else {
                e = Easter.easter(y);
            }
            rslt[i] = e.format(DateTimeFormatter.ISO_DATE);
        }
        return rslt;
    }

    public Matrix longTermMean(Calendar calendar, int period) {
        double[][] ltm;
        if (calendar != null) {
            ltm = HolidaysUtility.longTermMean(calendar.getHolidays(), period);
        } else {
            ltm = new double[period][];
        }
        double[] means = GenericTradingDaysFactory.meanDays(period);
        FastMatrix M = FastMatrix.make(period, 7);
        for (int i = 0; i < period; ++i) {
            DataBlock row = M.row(i);
            row.set(means[i]);
            if (ltm[i] != null) {
                DataBlock C = DataBlock.of(ltm[i]);
                row.sub(C);
                row.add(6, C.sum());
            }
        }
        return M.unmodifiable();
    }

    public Matrix longTermMean(Calendar calendar, int period, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        FastMatrix M = FastMatrix.make(period, dc.getGroupsCount());
        Matrix m = longTermMean(calendar, period);
        for (int i = 0; i < M.getColumnsCount(); ++i) {
            DataBlock col = M.column(i);
            for (int j = 0; j < 7; ++j) {
                if (groups[j] == i) {
                    col.add(m.column(j));
                }
            }
        }
        return M.unmodifiable();
    }

    public Matrix holidays(Calendar calendar, String date, int length, int[] nonworking, String type) {
        LocalDate start = LocalDate.parse(date);
        Holiday[] elements = calendar.getHolidays();
        FastMatrix m = FastMatrix.make(length, elements.length);
        switch (type) {
            case "Skip":
                HolidaysUtility.fillDays(elements, m, start, nonworking, true);
                break;
            case "NextWorkingDay":
                HolidaysUtility.fillNextWorkingDays(elements, m, start, nonworking);
                break;
            case "PreviousWorkingDay":
                HolidaysUtility.fillPreviousWorkingDays(elements, m, start, nonworking);
                break;
            default:
                HolidaysUtility.fillDays(elements, m, start, nonworking, false);
        }
        return m.unmodifiable();
    }

}
