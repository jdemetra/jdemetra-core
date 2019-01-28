package demetra.calendars.internal;


@lombok.Value
public class Time {

    //
    // fields
    //
    int hour;
    int minute;
    double second;

    //
    // time conversion methods
    //
    public static double toMoment(int hour, int minute, double second) {
        return hour / 24d + minute / (24d * 60) + second / (24d * 60 * 60);
    }

    public double toMoment() {
        return toMoment(hour, minute, second);
    }


    /*- time-from-moment -*/
    public static Time fromMoment(double tee) {
        return new Time((int) Math.floor(DateUtility.mod(tee * 24, 24)),
                (int) Math.floor(DateUtility.mod(tee * 24 * 60, 60)),
                DateUtility.mod(tee * 24 * 60 * 60, 60));
    }
}
