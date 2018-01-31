/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdr.spec.ts;

import ec.tstoolkit.timeseries.Day;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Utility {

    public Day of(String date) throws ParseException {
        return Day.fromString(date);
    }

    public String toString(Date date) {
        StringBuilder builder = new StringBuilder();
        GregorianCalendar gc = new GregorianCalendar();
        builder.append(gc.get(GregorianCalendar.YEAR)).append('-')
                .append(gc.get(GregorianCalendar.MONTH)).append('-')
                .append(gc.get(GregorianCalendar.DAY_OF_MONTH));
        return builder.toString();
    }

    public String toString(Day day) {
        return day == null || day == Day.BEG || day == Day.END ? "" : toString(day.getTime());
    }

}
