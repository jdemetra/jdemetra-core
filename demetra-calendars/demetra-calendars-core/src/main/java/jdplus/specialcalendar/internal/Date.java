package jdplus.specialcalendar.internal;

import java.time.LocalDate;

public interface Date {

    /**
     * date conversion method
     *
     * @return
     */
    long toFixed();

    default LocalDate toLocalDate() {
        return fixedToLocalDate(toFixed());    
    }

    static LocalDate fixedToLocalDate(long l) {
        Gregorian gregorian = Gregorian.fromFixed(l);
        return LocalDate.of((int) gregorian.getYear(), gregorian.getMonth(), gregorian.getDay());
    }
}
