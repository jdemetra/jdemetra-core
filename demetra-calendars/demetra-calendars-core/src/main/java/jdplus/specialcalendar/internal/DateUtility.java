package jdplus.specialcalendar.internal;

@lombok.experimental.UtilityClass
public class DateUtility  {

    final long[] LEMPTY=new long[0];
    //
    // constants
    //
    public final int JANUARY = 1;
    public final int FEBRUARY = 2;
    public final int MARCH = 3;
    public final int APRIL = 4;
    public final int MAY = 5;
    public final int JUNE = 6;
    public final int JULY = 7;
    public final int AUGUST = 8;
    public final int SEPTEMBER = 9;
    public final int OCTOBER = 10;
    public final int NOVEMBER = 11;
    public final int DECEMBER = 12;

    public final int SUNDAY = 0;
    public final int MONDAY = 1;
    public final int TUESDAY = 2;
    public final int WEDNESDAY = 3;
    public final int THURSDAY = 4;
    public final int FRIDAY = 5;
    public final int SATURDAY = 6;


    /*- jd-epoch -*/
    // TYPE moment
    // Fixed time of start of the julian day number.
    public final double JD_EPOCH = -1721424.5;


    /*- mjd-epoch -*/
    // TYPE fixed-date
    // Fixed time of start of the modified julian day number.
    public final long MJD_EPOCH = 678576;


    /*- j2000 -*/
    // TYPE moment
    // Noon at start of Gregorian year 2000.
    public final double J2000 = hr(12) + Gregorian.toFixed(2000, JANUARY, 1);


    /*- mean-tropical-year -*/
    // TYPE real
    public final double MEAN_TROPICAL_YEAR = 365.242189;


    /*- mean-synodic-month -*/
    // TYPE real
    public final double MEAN_SYNODIC_MONTH = 29.530588853;


    /*- new -*/
    // TYPE phase
    // Excess of lunar longitude over solar longitude at new
    // moon.
    public final double NEW = deg(0);


    /*- first-quarter -*/
    // TYPE phase
    // Excess of lunar longitude over solar longitude at first
    // quarter moon.
    public final double FIRST_QUARTER = deg(90);


    /*- full -*/
    // TYPE phase
    // Excess of lunar longitude over solar longitude at full
    // moon.
    public final double FULL = deg(180);


    /*- last-quarter -*/
    // TYPE phase
    // Excess of lunar longitude over solar longitude at last
    // quarter moon.
    public final double LAST_QUARTER = deg(270);

    /*- spring -*/
    // TYPE season
    // Longitude of sun at vernal equinox.
    public final double SPRING = deg(0);


    /*- summer -*/
    // TYPE season
    // Longitude of sun at summer solstice.
    public final double SUMMER = deg(90);


    /*- autumn -*/
    // TYPE season
    // Longitude of sun at autumnal equinox.
    public final double AUTUMN = deg(180);


    /*- winter -*/
    // TYPE season
    // Longitude of sun at winter solstice.
    public final double WINTER = deg(270);

    /*- jerusalem -*/
    // TYPE location
    // Location of Jerusalem.
    public final Location JERUSALEM = new Location("Jerusalem, Israel", deg(31.8), deg(35.2), mt(800), 2);

    //
    // support methods
    //
    public double currentMoment() {
        return Gregorian.toFixed(1970, JANUARY, 1)
                + java.lang.System.currentTimeMillis() / (1000.0 * 60 * 60 * 24);
    }

    public long currentDate() {
        return (long) currentMoment();
    }


    /*- gregorian-date-difference -*/
    // TYPE (gregorian-date gregorian-date) -> integer
    // Number of days from Gregorian date $g-date1$ until
    // $g-date2$.
    public long difference(Date date1, Date date2) {
        return date2.toFixed() - date1.toFixed();
    }

    public long difference(long date1, Date date2) {
        return date2.toFixed() - date1;
    }

    public long difference(Date date1, long date2) {
        return date2 - date1.toFixed();
    }

    public long difference(long date1, long date2) {
        return date2 - date1;
    }

    public double mod(double x, double y) {
        return x - y * Math.floor(x / y);
    }

    public int mod(int x, int y) {
        return (int) (x - y * Math.floor((double) x / y));
    }

    public long mod(long x, long y) {
        return (long) (x - y * Math.floor((double) x / y));
    }


    /*- quotient -*/
    // TYPE (real non-zero-real) -> integer
    // Whole part of $m$/$n$.
    public long quotient(double x, double y) {
        return (long) Math.floor(x / y);
    }


    /*- adjusted-mod -*/
    // TYPE (real real) -> real
    // The value of ($x$ mod $y$) with $y$ instead of 0.
    public int adjustedMod(int x, int y) {
        return y + mod(x, -y);
    }

    public long adjustedMod(long x, long y) {
        return y + mod(x, -y);
    }

    public double adjustedMod(double x, double y) {
        return y + mod(x, -y);
    }


    /*- day-of-week-from-fixed -*/
    // TYPE fixed-date -> day-of-week
    // The residue class of the day of the week of $date$.
    public long dayOfWeekFromFixed(long date) {
        return mod(date, 7);
    }


    /*- kday-on-or-before -*/
    // TYPE (fixed-date weekday) -> fixed-date
    // Fixed date of the $k$-day on or before fixed $date$.
    // $k$=0 means Sunday, $k$=1 means Monday, and so on.
    public long kDayOnOrBefore(long date, int k) {
        return date - dayOfWeekFromFixed(date - k);
    }


    /*- kday-on-or-after -*/
    // TYPE (fixed-date weekday) -> fixed-date
    // Fixed date of the $k$-day on or after fixed $date$.
    // $k$=0 means Sunday, $k$=1 means Monday, and so on.
    public long kDayOnOrAfter(long date, int k) {
        return kDayOnOrBefore(date + 6, k);
    }


    /*- kday-nearest -*/
    // TYPE (fixed-date weekday) -> fixed-date
    // Fixed date of the $k$-day nearest fixed $date$.  
    // $k$=0 means Sunday, $k$=1 means Monday, and so on.
    public long kDayNearest(long date, int k) {
        return kDayOnOrBefore(date + 3, k);
    }


    /*- kday-after -*/
    // TYPE (fixed-date weekday) -> fixed-date
    // Fixed date of the $k$-day after fixed $date$.  
    // $k$=0 means Sunday, $k$=1 means Monday, and so on.
    public long kDayAfter(long date, int k) {
        return kDayOnOrBefore(date + 7, k);
    }


    /*- kday-before -*/
    // TYPE (fixed-date weekday) -> fixed-date
    // Fixed date of the $k$-day before fixed $date$.  
    // $k$=0 means Sunday, $k$=1 means Monday, and so on.
    public long kDayBefore(long date, int k) {
        return kDayOnOrBefore(date - 1, k);
    }


    /*- nth-kday -*/
    // TYPE (integer weekday gregorian-date) -> fixed-date
    // Fixed date of $n$-th $k$-day after Gregorian date.  If
    // $n$>0, return the $n$-th $k$-day on or after date.
    // If $n$<0, return the $n$-th $k$-day on or before date.
    // A $k$-day of 0 means Sunday, 1 means Monday, and so on.
    public long nthKDay(int n, int k, long gDate) {
        return n > 0
                ? kDayBefore(gDate, k) + 7 * n
                : kDayAfter(gDate, k) + 7 * n;
    }


    /*- first-kday -*/
    // TYPE (weekday gregorian-date -> fixed-date
    // Fixed date of first $k$-day on or after Gregorian date.
    // A $k$-day of 0 means Sunday, 1 means Monday, and so on.
    public long firstKDay(int k, long gDate) {
        return nthKDay(1, k, gDate);
    }


    /*- last-kday -*/
    // TYPE (weekday gregorian-date -> fixed-date
    // Fixed date of last $k$-day on or before Gregorian date.
    // A $k$-day of 0 means Sunday, 1 means Monday, and so on.
    public long lastKDay(int k, long gDate) {
        return nthKDay(-1, k, gDate);
    }

    public int signum(double x) {
        if (x < 0) {
            return -1;
        } else if (x > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public double square(double x) {
        return x * x;
    }


    /*- poly -*/
    // TYPE (real list-of-reals) -> real
    // Sum powers of $x$ with coefficients (from order 0 up)
    // in list $a$.
    public double poly(double x, double[] a) {
        double result = a[0];
        for (int i = 1; i < a.length; ++i) {
            result += a[i] * Math.pow(x, i);
        }
        return result;
    }

    /*- hr -*/
    // TYPE real -> interval
    // $x$ hours.
    public double hr(double x) {
        return x / 24;
    }

    /*- mt -*/
    // TYPE real -> distance
    // $x$ meters.
    public double mt(double x) {
        return x;
    }

    /*- deg -*/
    // TYPE real -> angle
    // TYPE list-of-reals -> list-of-angles
    // $x$ degrees.
    public double deg(double x) {
        return x;
    }

    public double[] deg(double[] x) {
        return x;
    }

    /*- angle -*/
    // TYPE (non-negative-integer
    // TYPE non-negative-integer real) -> angle
    // $x$ degrees, $m$ arcminutes, $s$ arcseconds.
    public double angle(double d, double m, double s) {
        return d + (m + s / 60) / 60;
    }


    /*- degrees -*/
    // TYPE real -> angle
    // Normalize angle $theta$ to range 0-360 degrees.
    public double degrees(double theta) {
        return mod(theta, 360);
    }


    /*- radians-to-degrees -*/
    // TYPE radian -> angle
    // Convert angle $theta$ from radians to degrees.
    public double radiansToDegrees(double theta) {
        return degrees(theta / Math.PI * 180);
    }


    /*- degrees-to-radians -*/
    // TYPE real -> radian
    // Convert angle $theta$ from degrees to radians.
    public double degreesToRadians(double theta) {
        return degrees(theta) * Math.PI / 180;
    }


    /*- sin-degrees -*/
    // TYPE angle -> amplitude
    // Sine of $theta$ (given in degrees).
    public double sinDegrees(double theta) {
        return Math.sin(degreesToRadians(theta));
    }


    /*- cosine-degrees -*/
    // TYPE angle -> amplitude
    // Cosine of $theta$ (given in degrees).
    public double cosDegrees(double theta) {
        return Math.cos(degreesToRadians(theta));
    }


    /*- tangent-degrees -*/
    // TYPE angle -> real
    // Tangent of $theta$ (given in degrees).
    public double tanDegrees(double theta) {
        return Math.tan(degreesToRadians(theta));
    }


    /*- arctan-degrees -*/
    // TYPE (real quadrant) -> angle
    // Arctangent of $x$ in degrees in quadrant $quad$.
    public double arcTanDegrees(double x, int quad) {
        double alpha = radiansToDegrees(Math.atan(x));
        return mod(quad == 1 || quad == 4 ? alpha : alpha + deg(180), 360);
    }


    /*- arcsin-degrees -*/
    // TYPE amplitude -> angle
    // Arcsine of $x$ in degrees.
    public double arcSinDegrees(double x) {
        return radiansToDegrees(Math.asin(x));
    }


    /*- arccos-degrees -*/
    // TYPE amplitude -> angle
    // Arccosine of $x$ in degrees.
    public double arcCosDegrees(double x) {
        return radiansToDegrees(Math.acos(x));
    }

    /*- standard-from-universal -*/
    // TYPE (moment location) -> moment
    // Standard time from $tee_u$ in universal time at
    // $locale$.
    public double standardFromUniversal(double teeU, Location locale) {
        return teeU + locale .getZone() / 24;
    }

    /*- universal-from-standard -*/
    // TYPE (moment location) -> moment
    // Universal time from $tee_s$ in standard time at
    // $locale$.
    public double universalFromStandard(double teeS, Location locale) {
        return teeS - locale .getZone() / 24;
    }


    /*- local-from-universal -*/
    // TYPE (moment location) -> moment
    // Local time from universal $tee_u$ at $locale$.
    public double localFromUniversal(double teeU, Location locale) {
        return teeU + locale.getLongitude() / deg(360);
    }


    /*- universal-from-local -*/
    // TYPE (moment location) -> moment
    // Universal time from local $tee_ell$ at $locale$.
    public double universalFromLocal(double teeEll, Location locale) {
        return teeEll - locale.getLongitude() / deg(360);
    }


    /*- standard-from-local -*/
    // TYPE (moment location) -> moment
    // Standard time from local $tee_ell$ at $locale$.
    public double standardFromLocal(double teeEll, Location locale) {
        return standardFromUniversal(universalFromLocal(teeEll, locale), locale);
    }


    /*- local-from-standard -*/
    // TYPE (moment location) -> moment
    // Local time from standard $tee_s$ at $locale$.
    public double localFromStandard(double teeS, Location locale) {
        return localFromUniversal(universalFromStandard(teeS, locale), locale);
    }

    /*- midday -*/
    // TYPE (fixed-date location) -> moment
    // Standard time on fixed $date$ of midday at $locale$.
    public double midday(long date, Location locale) {
        return standardFromLocal(localFromApparent(date + hr(12)), locale);
    }

    /*- midnight -*/
    // TYPE (fixed-date location) -> moment
    // Standard time on fixed $date$ of true (apparent)
    // midnight at $locale$.
    public double midnight(long date, Location locale) {
        return standardFromLocal(localFromApparent(date), locale);
    }


    /*- moment-from-jd -*/
    // TYPE julian-day-number -> moment
    // Moment of julian day number $jd$.
    public double momentFromJD(double jd) {
        return jd + JD_EPOCH;
    }


    /*- jd-from-moment -*/
    // TYPE moment -> julian-day-number
    // Julian day number of moment $tee$.
    public double jdFromMoment(double tee) {
        return tee - JD_EPOCH;
    }


    /*- fixed-from-jd -*/
    // TYPE julian-day-number -> fixed-date
    // Fixed date of julian day number $jd$.
    public long fixedFromJD(double jd) {
        return (long) Math.floor(momentFromJD(jd));
    }


    /*- jd-from-fixed -*/
    // TYPE fixed-date -> julian-day-number
    // Julian day number of fixed $date$.
    public double jdFromFixed(long date) {
        return jdFromMoment(date);
    }


    /*- fixed-from-mjd -*/
    // TYPE julian-day-number -> fixed-date
    // Fixed date of modified julian day number $mjd$.
    public long fixedFromMJD(double mjd) {
        return (long) Math.floor(mjd + MJD_EPOCH);
    }


    /*- mjd-from-fixed -*/
    // TYPE fixed-date -> julian-day-number
    // Modified julian day number of fixed $date$.
    public double mjdFromFixed(long date) {
        return date - MJD_EPOCH;
    }

    /*- direction -*/
    // TYPE (location location) -> angle
    // Angle (clockwise from North)
    // to face $focus$ when standing in $locale$.
    // Subject to errors near focus and its antipode.
    public double direction(Location locale, Location focus) {
        double phi = locale.getLatitude();
        double phiPrime = focus.getLatitude();
        double psi = locale.getLongitude();
        double psiPrime = focus.getLongitude();
        double denom = cosDegrees(phi) * tanDegrees(phiPrime) - sinDegrees(phi) * cosDegrees(psi - psiPrime);
        return denom == 0.0
                ? 0
                : mod(arcTanDegrees(
                        sinDegrees(psiPrime - psi) / denom,
                        denom < 0 ? 2 : 1
                ),
                         360);
    }


    /*- julian-centuries -*/
    // TYPE moment -> real
    // Julian centuries since 2000 at moment $tee$.
    public double julianCenturies(double tee) {
        return (dynamicalFromUniversal(tee) - J2000) / 36525;
    }

    /*- obliquity -*/
    // TYPE moment -> angle
    // Obliquity of ecliptic at moment $tee$.
    public double obliquity(double tee) {
        double c = julianCenturies(tee);
        return angle(23, 26, 21.448) + poly(c, ob.coeffObliquity);
    }

    @lombok.experimental.UtilityClass
    private static class ob {

        private final double[] coeffObliquity = new double[]{0, angle(0, 0, -46.8150), angle(0, 0, -0.00059), angle(0, 0, 0.001813)};
    }

    /*- moment-from-depression -*/
    // TYPE (moment location angle) -> moment
    // Moment in Local Time near $approx$ when depression
    // angle of sun is $alpha$ (negative if above horizon) at
    // $locale$; bogus if never occurs.
    public double momentFromDepression(double approx, Location locale, double alpha)
            throws BogusTimeException {
        double phi = locale.getLatitude();
        double tee = universalFromLocal(approx, locale);
        double delta = arcSinDegrees(sinDegrees(obliquity(tee)) * sinDegrees(solarLongitude(tee)));
        boolean morning = mod(approx, 1) < 0.5;
        double sineOffset = tanDegrees(phi) * tanDegrees(delta)
                + sinDegrees(alpha) / (cosDegrees(delta) * cosDegrees(phi));
        double offset = mod(0.5 + arcSinDegrees(sineOffset) / deg(360), 1) - 0.5;
        if (Math.abs(sineOffset) > 1) {
            throw new BogusTimeException();
        }
        return localFromApparent(Math.floor(approx) + (morning ? .25 - offset : .75 + offset));
    }

    /*- dawn -*/
    // TYPE (fixed-date location angle) -> moment
    // Standard time in morning of $date$ at $locale$
    // when depression angle of sun is $alpha$.
    public double dawn(long date, Location locale, double alpha)
            throws BogusTimeException {
        double approx;
        try {
            approx = momentFromDepression(date + .25, locale, alpha);
        } catch (BogusTimeException ex) {
            approx = date;
        }
        double result = momentFromDepression(approx, locale, alpha);
        return standardFromLocal(result, locale);
    }

    /*- dusk -*/
    // TYPE (fixed-date location angle) -> moment
    // Standard time in evening on $date$ at $locale$
    // when depression angle of sun is $alpha$.
    public double dusk(long date, Location locale, double alpha)
            throws BogusTimeException {
        double approx;
        try {
            approx = momentFromDepression(date + .75, locale, alpha);
        } catch (BogusTimeException ex) {
            approx = date + .99d;
        }
        double result = momentFromDepression(approx, locale, alpha);
        return standardFromLocal(result, locale);
    }


    /*- sunrise -*/
    // TYPE (fixed-date location) -> moment
    // Standard time of sunrise on $date$ at $locale$.
    public double sunrise(long date, Location locale)
            throws BogusTimeException {
        double h = Math.max(0, locale.getElevation());
        final double capR = mt(6.372E+6);
        double dip = arcCosDegrees(capR / (capR + h));
        double alpha = angle(0, 50, 0) + dip;
        return dawn(date, locale, alpha);
    }


    /*- sunset -*/
    // TYPE (fixed-date location) -> moment
    // Standard time of sunset on fixed $date$ at $locale$.
    public double sunset(long date, Location locale)
            throws BogusTimeException {
        double h = Math.max(0, locale.getElevation());
        final double capR = mt(6.372E+6);
        double dip = arcCosDegrees(capR / (capR + h));
        double alpha = angle(0, 50, 0) + dip;
        return dusk(date, locale, alpha);
    }

    /*- temporal-hour -*/
    // TYPE (fixed-date location) -> real
    // Length of daytime temporal hour on fixed $date$ at
    // $locale$.
    public double temporalHour(long date, Location locale)
            throws BogusTimeException {
        return (sunset(date, locale) - sunrise(date, locale)) / 12;
    }

    /*- standard-from-sundial -*/
    // TYPE (fixed-date real location) -> moment
    // Standard time on fixed $date$ of temporal $hour$ at
    // $locale$.
    public double standardFromSundial(long date, double hour, Location locale)
            throws BogusTimeException {
        double tee = temporalHour(date, locale);
        return sunrise(date, locale) + ((6 <= hour && hour <= 18) ? (hour - 6) * tee : (hour - 6) * (1d / 12 - tee));
    }


    /*- universal-from-dynamical -*/
    // TYPE moment -> moment
    // Universal moment from Dynamical time $tee$.
    public double universalFromDynamical(double tee) {
        return tee - ephemerisCorrection(tee);
    }


    /*- dynamical-from-universal -*/
    // TYPE moment -> moment
    // Dynamical time at Universal moment $tee$.
    public double dynamicalFromUniversal(double tee) {
        return tee + ephemerisCorrection(tee);
    }


    /*- ephemeris-correction -*/
    // TYPE moment -> fraction-of-day
    // Dynamical Time minus Universal Time (in days) for
    // fixed time $tee$.  Adapted from "Astronomical Algorithms"
    // by Jean Meeus, Willmann-Bell, Inc., 1991.
    public double ephemerisCorrection(double tee) {
        long year = Gregorian.yearFromFixed((long) Math.floor(tee));
        double c = difference(Gregorian.toFixed(1900, JANUARY, 1), Gregorian.toFixed(year, JULY, 1)) / 36525d;
        double result;
        if (1988 <= year && year <= 2019) {
            result = (year - 1933) / (24d * 60 * 60);
        } else if (1900 <= year && year <= 1987) {
            result = poly(c, ec.coeff19th);
        } else if (1800 <= year && year <= 1899) {
            result = poly(c, ec.coeff18th);
        } else if (1700 <= year && year <= 1799) {
            result = poly(year - 1700, ec.coeff17th) / (24 * 60 * 60);
        } else if (1620 <= year && year <= 1699) {
            result = poly(year - 1600, ec.coeff16th) / (24 * 60 * 60);
        } else {
            double x = hr(12) + difference(Gregorian.toFixed(1810, JANUARY, 1), Gregorian.toFixed(year, JANUARY, 1));
            return (x * x / 41048480 - 15) / (24 * 60 * 60);
        }
        return result;
    }

    @lombok.experimental.UtilityClass
    private static class ec {

        private final double[] coeff19th = new double[]{-0.00002, 0.000297, 0.025184, -0.181133, 0.553040, -0.861938, 0.677066, -0.212591};
        private final double[] coeff18th = new double[]{-0.000009, 0.003844, 0.083563, 0.865736, 4.867575, 15.845535, 31.332267, 38.291999, 28.316289, 11.636204, 2.043794};
        private final double[] coeff17th = new double[]{8.118780842, -0.005092142, 0.003336121, -0.0000266484};
        private final double[] coeff16th = new double[]{196.58333, -4.0675, 0.0219167};
    }

    /*- equation-of-time -*/
    // TYPE moment -> fraction-of-day
    // Equation of time (as fraction of day) for moment $tee$.
    // Adapted from "Astronomical Algorithms" by Jean Meeus,
    // Willmann-Bell, Inc., 1991.
    public double equationOfTime(double tee) {
        double c = julianCenturies(tee);
        double longitude = poly(c, et.coeffLongitude);
        double anomaly = poly(c, et.coeffAnomaly);
        double eccentricity = poly(c, et.coeffEccentricity);
        double varepsilon = obliquity(tee);
        double y = square(tanDegrees(varepsilon / 2));
        double equation = (1d / 2d / Math.PI) * (y * sinDegrees(2 * longitude)
                + -2 * eccentricity * sinDegrees(anomaly)
                + 4 * eccentricity * y * sinDegrees(anomaly) * cosDegrees(2 * longitude)
                + -0.5 * y * y * sinDegrees(4 * longitude)
                + -1.25 * eccentricity * eccentricity * sinDegrees(2 * anomaly));
        return signum(equation) * Math.min(Math.abs(equation), hr(12));
    }

    @lombok.experimental.UtilityClass
    private static class et {

        private final double[] coeffLongitude = deg(new double[]{280.46645, 36000.76983, 0.0003032});
        private final double[] coeffAnomaly = deg(new double[]{357.52910, 35999.05030, -0.0001559, -0.00000048});
        private final double[] coeffEccentricity = deg(new double[]{0.016708617, -0.000042037, -0.0000001236});
    }


    /*- local-from-apparent -*/
    // TYPE moment -> moment
    // Local time from sundial time $tee$.
    public double localFromApparent(double tee) {
        return tee - equationOfTime(tee);
    }


    /*- apparent-from-local -*/
    // TYPE moment -> moment
    // Sundial time at local time $tee$.
    public double apparentFromLocal(double tee) {
        return tee + equationOfTime(tee);
    }


    /*- solar-longitude -*/
    // TYPE moment -> season
    // Longitude of sun at moment $tee$.
    // Adapted from "Planetary Programs and Tables from -4000
    // to +2800" by Pierre Bretagnon and Jean-Louis Simon,
    // Willmann-Bell, Inc., 1986.
    public double solarLongitude(double tee) {
        double c = julianCenturies(tee);
        double sigma = 0;
        for (int i = 0; i < sl.coefficients.length; ++i) {
            sigma += sl.coefficients[i] * sinDegrees(sl.multipliers[i] * c + sl.addends[i]);
        }
        double longitude = deg(282.7771834)
                + 36000.76953744 * c
                + 0.000005729577951308232 * sigma;
        return mod(longitude + aberration(tee) + nutation(tee), 360);
    }

    @lombok.experimental.UtilityClass
    private static class sl {

        private final int[] coefficients = new int[]{
            403406, 195207, 119433, 112392, 3891, 2819, 1721,
            660, 350, 334, 314, 268, 242, 234, 158, 132, 129, 114,
            99, 93, 86, 78, 72, 68, 64, 46, 38, 37, 32, 29, 28, 27, 27,
            25, 24, 21, 21, 20, 18, 17, 14, 13, 13, 13, 12, 10, 10, 10,
            10
        };
        private final double[] multipliers = new double[]{
            0.9287892, 35999.1376958, 35999.4089666,
            35998.7287385, 71998.20261, 71998.4403,
            36000.35726, 71997.4812, 32964.4678,
            -19.4410, 445267.1117, 45036.8840, 3.1008,
            22518.4434, -19.9739, 65928.9345,
            9038.0293, 3034.7684, 33718.148, 3034.448,
            -2280.773, 29929.992, 31556.493, 149.588,
            9037.750, 107997.405, -4444.176, 151.771,
            67555.316, 31556.080, -4561.540,
            107996.706, 1221.655, 62894.167,
            31437.369, 14578.298, -31931.757,
            34777.243, 1221.999, 62894.511,
            -4442.039, 107997.909, 119.066, 16859.071,
            -4.578, 26895.292, -39.127, 12297.536,
            90073.778
        };
        private final double[] addends = new double[]{
            270.54861, 340.19128, 63.91854, 331.26220,
            317.843, 86.631, 240.052, 310.26, 247.23,
            260.87, 297.82, 343.14, 166.79, 81.53,
            3.50, 132.75, 182.95, 162.03, 29.8,
            266.4, 249.2, 157.6, 257.8, 185.1, 69.9,
            8.0, 197.1, 250.4, 65.3, 162.7, 341.5,
            291.6, 98.5, 146.7, 110.0, 5.2, 342.6,
            230.9, 256.1, 45.3, 242.9, 115.2, 151.8,
            285.3, 53.3, 126.6, 205.7, 85.9,
            146.1
        };
    }


    /*- nutation -*/
    // TYPE moment -> angle
    // Longitudinal nutation at moment $tee$.
    public double nutation(double tee) {
        double c = julianCenturies(tee);
        double capA = poly(c, nu.coeffa);
        double capB = poly(c, nu.coeffb);
        return deg(-0.004778) * sinDegrees(capA)
                + deg(-0.0003667) * sinDegrees(capB);
    }

    @lombok.experimental.UtilityClass
    private static class nu {

        private final double[] coeffa = deg(new double[]{124.90, -1934.134, 0.002063});
        private final double[] coeffb = deg(new double[]{201.11, 72001.5377, 0.00057});
    }


    /*- aberration -*/
    // TYPE moment -> angle
    // Aberration at moment $tee$.
    public double aberration(double tee) {
        double c = julianCenturies(tee);
        return deg(0.0000974) * cosDegrees(deg(177.63) + deg(35999.01848) * c) - deg(0.005575);
    }


    /*- solar-longitude-after -*/
    // TYPE (moment season) -> moment
    // Moment UT of the first time at or after $tee$
    // when the solar longitude will be $phi$ degrees.
    public double solarLongitudeAfter(double tee, double phi) {
        double varepsilon = 0.00001;
        double rate = MEAN_TROPICAL_YEAR / deg(360);
        double tau = tee + rate * mod(phi - solarLongitude(tee), 360);
        double l = Math.max(tee, tau - 5);
        double u = tau + 5;

        double lo = l, hi = u, x = (hi + lo) / 2;
        while (hi - lo > varepsilon) {
            if (mod(solarLongitude(x) - phi, 360) < deg(180)) {
                hi = x;
            } else {
                lo = x;
            }

            x = (hi + lo) / 2;
        }
        return x;
    }


    /*- lunar-longitude -*/
    // TYPE moment -> angle
    // Longitude of moon (in degrees) at moment $tee$.
    // Adapted from "Astronomical Algorithms" by Jean Meeus,
    // Willmann-Bell, Inc., 1991.
    public double lunarLongitude(double tee) {
        double c = julianCenturies(tee);
        double meanMoon = degrees(poly(c, llon.coeffMeanMoon));
        double elongation = degrees(poly(c, llon.coeffElongation));
        double solarAnomaly = degrees(poly(c, llon.coeffSolarAnomaly));
        double lunarAnomaly = degrees(poly(c, llon.coeffLunarAnomaly));
        double moonNode = degrees(poly(c, llon.coeffMoonNode));
        double capE = poly(c, llon.coeffCapE);
        double sigma = 0;
        for (int i = 0; i < llon.argsLunarElongation.length; ++i) {
            double x = llon.argsSolarAnomaly[i];
            sigma += llon.sineCoefficients[i]
                    * Math.pow(capE, Math.abs(x))
                    * sinDegrees(llon.argsLunarElongation[i] * elongation
                            + x * solarAnomaly
                            + llon.argsLunarAnomaly[i] * lunarAnomaly
                            + llon.argsMoonFromNode[i] * moonNode);
        }
        double correction = (deg(1) / 1000000) * sigma;
        double venus = (deg(3958) / 1000000) * sinDegrees(119.75 + c * 131.849);
        double jupiter = (deg(318) / 1000000) * sinDegrees(53.09 + c * 479264.29);
        double flatEarth = (deg(1962) / 1000000) * sinDegrees(meanMoon - moonNode);
        return mod(meanMoon + correction + venus + jupiter + flatEarth + nutation(tee), 360);
    }

    @lombok.experimental.UtilityClass
    private static class llon {

        private final double[] coeffMeanMoon = deg(new double[]{218.3164591, 481267.88134236, -0.0013268, 1d / 538841, -1d / 65194000});
        private final double[] coeffElongation = deg(new double[]{297.8502042, 445267.1115168, -0.00163, 1d / 545868, -1d / 113065000});
        private final double[] coeffSolarAnomaly = deg(new double[]{357.5291092, 35999.0502909, -0.0001536, 1d / 24490000});
        private final double[] coeffLunarAnomaly = deg(new double[]{134.9634114, 477198.8676313, 0.008997, 1d / 69699, -1d / 14712000});
        private final double[] coeffMoonNode = deg(new double[]{93.2720993, 483202.0175273, -0.0034029, -1d / 3526000, 1d / 863310000});
        private final double[] coeffCapE = new double[]{1, -0.002516, -0.0000074};
        private final byte[] argsLunarElongation = new byte[]{
            0, 2, 2, 0, 0, 0, 2, 2, 2, 2, 0, 1, 0, 2, 0, 0, 4, 0, 4, 2, 2, 1,
            1, 2, 2, 4, 2, 0, 2, 2, 1, 2, 0, 0, 2, 2, 2, 4, 0, 3, 2, 4, 0, 2,
            2, 2, 4, 0, 4, 1, 2, 0, 1, 3, 4, 2, 0, 1, 2
        };
        private final byte[] argsSolarAnomaly = new byte[]{
            0, 0, 0, 0, 1, 0, 0, -1, 0, -1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1,
            0, 1, -1, 0, 0, 0, 1, 0, -1, 0, -2, 1, 2, -2, 0, 0, -1, 0, 0, 1,
            -1, 2, 2, 1, -1, 0, 0, -1, 0, 1, 0, 1, 0, 0, -1, 2, 1, 0
        };
        private final byte[] argsLunarAnomaly = new byte[]{
            1, -1, 0, 2, 0, 0, -2, -1, 1, 0, -1, 0, 1, 0, 1, 1, -1, 3, -2,
            -1, 0, -1, 0, 1, 2, 0, -3, -2, -1, -2, 1, 0, 2, 0, -1, 1, 0,
            -1, 2, -1, 1, -2, -1, -1, -2, 0, 1, 4, 0, -2, 0, 2, 1, -2, -3,
            2, 1, -1, 3
        };
        private final byte[] argsMoonFromNode = new byte[]{
            0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, -2, 2, -2, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, -2, 2, 0, 2, 0, 0, 0, 0,
            0, 0, -2, 0, 0, 0, 0, -2, -2, 0, 0, 0, 0, 0, 0, 0
        };
        private final int[] sineCoefficients = new int[]{
            6288774, 1274027, 658314, 213618, -185116, -114332,
            58793, 57066, 53322, 45758, -40923, -34720, -30383,
            15327, -12528, 10980, 10675, 10034, 8548, -7888,
            -6766, -5163, 4987, 4036, 3994, 3861, 3665, -2689,
            -2602, 2390, -2348, 2236, -2120, -2069, 2048, -1773,
            -1595, 1215, -1110, -892, -810, 759, -713, -700, 691,
            596, 549, 537, 520, -487, -399, -381, 351, -340, 330,
            327, -323, 299, 294
        };
    }


    /*- nth-new-moon -*/
    // TYPE integer -> moment
    // Moment of $n$-th new moon after (or before) the new moon
    // of January 11, 1.  Adapted from "Astronomical
    // Algorithms" by Jean Meeus, Willmann-Bell, Inc., 1991.
    public double nthNewMoon(long n) {
        double k = n - 24724;
        double c = k / 1236.85;
        double approx = poly(c, nm.coeffApprox);
        double capE = poly(c, nm.coeffCapE);
        double solarAnomaly = poly(c, nm.coeffSolarAnomaly);
        double lunarAnomaly = poly(c, nm.coeffLunarAnomaly);
        double moonArgument = poly(c, nm.coeffMoonArgument);
        double capOmega = poly(c, nm.coeffCapOmega);
        double correction = -0.00017 * sinDegrees(capOmega);
        for (int ix = 0; ix < nm.sineCoeff.length; ++ix) {
            correction += nm.sineCoeff[ix] * Math.pow(capE, nm.EFactor[ix])
                    * sinDegrees(nm.solarCoeff[ix] * solarAnomaly
                            + nm.lunarCoeff[ix] * lunarAnomaly
                            + nm.moonCoeff[ix] * moonArgument);
        }
        double additional = 0;
        for (int ix = 0; ix < nm.addConst.length; ++ix) {
            additional += nm.addFactor[ix]
                    * sinDegrees(nm.addConst[ix] + nm.addCoeff[ix] * k);
        }
        double extra = 0.000325 * sinDegrees(poly(c, nm.extra));
        return universalFromDynamical(approx + correction + extra + additional);
    }

    @lombok.experimental.UtilityClass
    private static class nm {

        private final double[] coeffApprox = new double[]{730125.59765, MEAN_SYNODIC_MONTH * 1236.85, 0.0001337, -0.000000150, 0.00000000073};
        private final double[] coeffCapE = new double[]{1, -0.002516, -0.0000074};
        private final double[] coeffSolarAnomaly = deg(new double[]{2.5534, 29.10535669 * 1236.85, -0.0000218, -0.00000011});
        private final double[] coeffLunarAnomaly = deg(new double[]{201.5643, 385.81693528 * 1236.85, 0.0107438, 0.00001239, -0.000000058});
        private final double[] coeffMoonArgument = deg(new double[]{160.7108, 390.67050274 * 1236.85, -0.0016341, -0.00000227, 0.000000011});
        private final double[] coeffCapOmega = new double[]{124.7746, -1.56375580 * 1236.85, 0.0020691, 0.00000215};
        private final byte[] EFactor = new byte[]{0, 1, 0, 0, 1, 1, 2, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        private final byte[] solarCoeff = new byte[]{0, 1, 0, 0, -1, 1, 2, 0, 0, 1, 0, 1, 1, -1, 2, 0, 3, 1, 0, 1, -1, -1, 1, 0};
        private final byte[] lunarCoeff = new byte[]{1, 0, 2, 0, 1, 1, 0, 1, 1, 2, 3, 0, 0, 2, 1, 2, 0, 1, 2, 1, 1, 1, 3, 4};
        private final byte[] moonCoeff = new byte[]{0, 0, 0, 2, 0, 0, 0, -2, 2, 0, 0, 2, -2, 0, 0, -2, 0, -2, 2, 2, 2, -2, 0, 0};
        private final double[] sineCoeff = new double[]{
            -0.40720, 0.17241, 0.01608, 0.01039, 0.00739, -0.00514, 0.00208,
            -0.00111, -0.00057, 0.00056, -0.00042, 0.00042, 0.00038, -0.00024,
            -0.00007, 0.00004, 0.00004, 0.00003, 0.00003, -0.00003, 0.00003,
            -0.00002, -0.00002, 0.00002
        };
        private final double[] addConst = new double[]{
            251.88, 251.83, 349.42, 84.66, 141.74, 207.14, 154.84, 34.52, 207.19,
            291.34, 161.72, 239.56, 331.55
        };
        private final double[] addCoeff = new double[]{
            0.016321, 26.641886, 36.412478, 18.206239, 53.303771, 2.453732,
            7.306860, 27.261239, 0.121824, 1.844379, 24.198154, 25.513099, 3.592518
        };
        private final double[] addFactor = new double[]{
            0.000165, 0.000164, 0.000126, 0.000110, 0.000062, 0.000060, 0.000056,
            0.000047, 0.000042, 0.000040, 0.000037, 0.000035, 0.000023
        };
        private final double[] extra = deg(new double[]{
            299.77, 132.8475848, -0.009173
        });
    }


    /*- new-moon-before -*/
    // TYPE moment -> moment
    // Moment UT of last new moon before $tee$.
    public double newMoonBefore(double tee) {
        double t0 = nthNewMoon(0);
        double phi = lunarPhase(tee);
        long n = Math.round((tee - t0) / MEAN_SYNODIC_MONTH - phi / deg(360));
        long k = n - 1;
        for (; nthNewMoon(k) < tee; ++k);
        k--;
        return nthNewMoon(k);
    }

    /*- new-moon-after -*/
    // TYPE moment -> moment
    // Moment UT of first new moon at or after $tee$.
    public double newMoonAfter(double tee) {
        double t0 = nthNewMoon(0);
        double phi = lunarPhase(tee);
        long n = Math.round((tee - t0) / MEAN_SYNODIC_MONTH - phi / deg(360));
        long k = n;
        for (; !(nthNewMoon(k) >= tee); ++k);
        return nthNewMoon(k);
    }

    /*- lunar-phase -*/
    // TYPE moment -> phase
    // Lunar phase, as an angle in degrees, at moment $tee$.
    // An angle of 0 means a new moon, 90 degrees means the
    // first quarter, 180 means a full moon, and 270 degrees
    // means the last quarter.
    public double lunarPhase(double tee) {
        return mod(lunarLongitude(tee) - solarLongitude(tee), 360);
    }

    /*- lunar-phase-before -*/
    // TYPE (moment phase) -> moment
    // Moment UT of the last time at or before $tee$
    // when the lunar-phase was $phi$ degrees.
    public double lunarPhaseBefore(double tee, double phi) {
        double varepsilon = 0.00001;
        double tau = tee - MEAN_SYNODIC_MONTH * (1d / 360) * mod(lunarPhase(tee) - phi, deg(360));
        double l = tau - 2;
        double u = Math.min(tee, tau + 2);

        double lo = l, hi = u, x = (hi + lo) / 2;
        while (hi - lo > varepsilon) {
            if (mod(lunarPhase(x) - phi, 360) < deg(180)) {
                hi = x;
            } else {
                lo = x;
            }

            x = (hi + lo) / 2;
        }
        return x;
    }

    /*- lunar-phase-after -*/
    // TYPE (moment phase) -> moment
    // Moment UT of the next time at or after $tee$
    // when the lunar-phase is $phi$ degrees.
    public double lunarPhaseAfter(double tee, double phi) {
        double varepsilon = 0.00001;
        double tau = tee + MEAN_SYNODIC_MONTH * (1d / 360) * mod(phi - lunarPhase(tee), deg(360));
        double l = Math.max(tee, tau - 2);
        double u = tau + 2;

        double lo = l, hi = u, x = (hi + lo) / 2;
        while (hi - lo > varepsilon) {
            if (mod(lunarPhase(x) - phi, 360) < deg(180)) {
                hi = x;
            } else {
                lo = x;
            }

            x = (hi + lo) / 2;
        }
        return x;
    }


    /*- lunar-latitude -*/
    // TYPE moment -> angle
    // Latitude of moon (in degrees) at moment $tee$.
    // Adapted from "Astronomical Algorithms" by Jean Meeus,
    // Willmann-Bell, Inc., 1998.
    public double lunarLatitude(double tee) {
        double c = julianCenturies(tee);
        double longitude = degrees(poly(c, llat.coeffLongitude));
        double elongation = degrees(poly(c, llat.coeffElongation));
        double solarAnomaly = degrees(poly(c, llat.coeffSolarAnomaly));
        double lunarAnomaly = degrees(poly(c, llat.coeffLunarAnomaly));
        double moonNode = degrees(poly(c, llat.coeffMoonNode));
        double capE = poly(c, llat.coeffCapE);
        double latitude = 0;
        for (int i = 0; i < llat.argsLunarElongation.length; ++i) {
            double x = llat.argsSolarAnomaly[i];
            latitude += llat.sineCoefficients[i]
                    * Math.pow(capE, Math.abs(x))
                    * sinDegrees(llat.argsLunarElongation[i] * elongation
                            + x * solarAnomaly
                            + llat.argsLunarAnomaly[i] * lunarAnomaly
                            + llat.argsMoonNode[i] * moonNode);
        }
        latitude *= deg(1) / 1000000;
        double venus = (deg(175) / 1000000) * (sinDegrees(deg(119.75) + c * 131.849 + moonNode) + sinDegrees(deg(119.75) + c * 131.849 - moonNode));
        double flatEarth = (deg(-2235) / 1000000) * sinDegrees(longitude)
                + (deg(127) / 1000000) * sinDegrees(longitude - lunarAnomaly)
                + (deg(-115) / 1000000) * sinDegrees(longitude + lunarAnomaly);
        double extra = (deg(382) / 1000000) * sinDegrees(deg(313.45) + c * deg(481266.484));
        return mod(latitude + venus + flatEarth + extra, 360);
    }

    @lombok.experimental.UtilityClass
    private static class llat {

        private final double[] coeffLongitude = deg(new double[]{218.3164591, 481267.88134236, -0.0013268, 1d / 538841, -1d / 65194000});
        private final double[] coeffElongation = deg(new double[]{297.8502042, 445267.1115168, -0.00163, 1d / 545868, -1d / 113065000});
        private final double[] coeffSolarAnomaly = deg(new double[]{357.5291092, 35999.0502909, -0.0001536, 1d / 24490000});
        private final double[] coeffLunarAnomaly = deg(new double[]{134.9634114, 477198.8676313, 0.008997, 1d / 69699, -1d / 14712000});
        private final double[] coeffMoonNode = deg(new double[]{93.2720993, 483202.0175273, -0.0034029, -1d / 3526000, 1d / 863310000});
        private final double[] coeffCapE = new double[]{1, -0.002516, -0.0000074};
        private final byte[] argsLunarElongation = new byte[]{
            0, 0, 0, 2, 2, 2, 2, 0, 2, 0, 2, 2, 2, 2, 2, 2, 2, 0, 4, 0, 0, 0,
            1, 0, 0, 0, 1, 0, 4, 4, 0, 4, 2, 2, 2, 2, 0, 2, 2, 2, 2, 4, 2, 2,
            0, 2, 1, 1, 0, 2, 1, 2, 0, 4, 4, 1, 4, 1, 4, 2};
        private final byte[] argsSolarAnomaly = new byte[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 1, -1, -1, -1, 1, 0, 1,
            0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1, 1,
            0, -1, -2, 0, 1, 1, 1, 1, 1, 0, -1, 1, 0, -1, 0, 0, 0, -1, -2};
        private final byte[] argsLunarAnomaly = new byte[]{
            0, 1, 1, 0, -1, -1, 0, 2, 1, 2, 0, -2, 1, 0, -1, 0, -1, -1, -1,
            0, 0, -1, 0, 1, 1, 0, 0, 3, 0, -1, 1, -2, 0, 2, 1, -2, 3, 2, -3,
            -1, 0, 0, 1, 0, 1, 1, 0, 0, -2, -1, 1, -2, 2, -2, -1, 1, 1, -2,
            0, 0};
        private final byte[] argsMoonNode = new byte[]{
            1, 1, -1, -1, 1, -1, 1, 1, -1, -1, -1, -1, 1, -1, 1, 1, -1, -1,
            -1, 1, 3, 1, 1, 1, -1, -1, -1, 1, -1, 1, -3, 1, -3, -1, -1, 1,
            -1, 1, -1, 1, 1, 1, 1, -1, 3, -1, -1, 1, -1, -1, 1, -1, 1, -1,
            -1, -1, -1, -1, -1, 1};
        private final int[] sineCoefficients = new int[]{
            5128122, 280602, 277693, 173237, 55413, 46271, 32573,
            17198, 9266, 8822, 8216, 4324, 4200, -3359, 2463, 2211,
            2065, -1870, 1828, -1794, -1749, -1565, -1491, -1475,
            -1410, -1344, -1335, 1107, 1021, 833, 777, 671, 607,
            596, 491, -451, 439, 422, 421, -366, -351, 331, 315,
            302, -283, -229, 223, 223, -220, -220, -185, 181,
            -177, 176, 166, -164, 132, -119, 115, 107};
    }


    /*- lunar-altitude -*/
    // TYPE (fixed-date location) -> angle
    // Altitude of moon at $tee$ at $locale$,
    // ignoring parallax and refraction.
    // Adapted from "Astronomical Algorithms" by Jean Meeus,
    // Willmann-Bell, Inc., 1998.
    public double lunarAltitude(double tee, Location locale) {
        double phi = locale.getLatitude();
        double psi = locale.getLongitude();
        double varepsilon = obliquity(tee);
        double lambda = lunarLongitude(tee);
        double beta = lunarLatitude(tee);
        double alpha = arcTanDegrees(
                (sinDegrees(lambda) * cosDegrees(varepsilon) - tanDegrees(beta) * sinDegrees(varepsilon))
                / cosDegrees(lambda),
                (int) quotient(lambda, deg(90)) + 1
        );
        double delta = arcSinDegrees(sinDegrees(beta) * cosDegrees(varepsilon)
                + cosDegrees(beta) * sinDegrees(varepsilon) * sinDegrees(lambda));
        double theta0 = siderealFromMoment(tee);
        double capH = mod(theta0 + psi - alpha, 360);
        double altitude = arcSinDegrees(sinDegrees(phi) * sinDegrees(delta) + cosDegrees(phi) * cosDegrees(delta) * cosDegrees(capH));
        return mod(altitude + deg(180), 360) - deg(180);
    }

    /*- estimate-prior-solar-longitude -*/
    // TYPE (moment season) -> moment
    // Approximate $moment$ at or before $tee$
    // when solar longitude just exceeded $phi$ degrees.
    public double estimatePriorSolarLongitude(double tee, double phi) {
        double rate = MEAN_TROPICAL_YEAR / deg(360);
        double tau = tee - rate * mod(solarLongitude(tee) - phi, 360);
        double capDelta = mod(solarLongitude(tau) - phi + deg(180), 360) - deg(180);
        return Math.min(tee, tau - rate * capDelta);
    }

    /*- visible-crescent -*/
    // TYPE (fixed-date location) -> boolean
    // S. K. Shaukat's criterion for likely
    // visibility of crescent moon on the eve of $date$ at $locale$.
    public boolean visibleCrescent(long date, Location locale)
            throws BogusTimeException {
        double tee = universalFromStandard(dusk(date - 1, locale, deg(4.5)), locale);
        double phase = lunarPhase(tee);
        double altitude = lunarAltitude(tee, locale);
        double arcOfLight = arcCosDegrees(cosDegrees(lunarLatitude(tee)) * cosDegrees(phase));
        return (NEW < phase && phase < FIRST_QUARTER)
                && (deg(10.6) <= arcOfLight && arcOfLight <= deg(90))
                && (altitude > deg(4.1));
    }

    /*- phasis-on-or-before -*/
    // TYPE (fixed-date location) -> fixed-date
    // Closest fixed date on or before $date$ when crescent
    // moon first became visible at $locale$.
    public long phasisOnOrBefore(long date, Location locale)
            throws BogusTimeException {
        long mean = (long) (date - Math.floor((lunarPhase(date) / deg(360)) * MEAN_SYNODIC_MONTH));
        long tau = (date - mean) <= 3 && !visibleCrescent(date, locale) ? mean - 30 : mean - 2;
        long d;
        for (d = tau; !visibleCrescent(d, locale); ++d);
        return d;
    }


    /*- sidereal-from-moment -*/
    // TYPE moment -> angle
    // Mean sidereal time of day from moment $tee$
    // expressed as hour angle.
    // Adapted from "Astronomical Algorithms"
    // by Jean Meeus, Willmann-Bell, Inc., 1991.
    public double siderealFromMoment(double tee) {
        double c = (tee - J2000) / 36525;
        return mod(poly(c, sfm.siderealCoeff), 360);
    }

    @lombok.experimental.UtilityClass
    private static class sfm {

        private final double[] siderealCoeff = deg(new double[]{280.46061837, 36525 * 360.98564736629, 0.000387933, 1d / 38710000});
    }

    public String nameFromNumber(long number, String[] nameList) {
        return nameList[(int) adjustedMod(number, nameList.length) - 1];
    }

    public String nameFromDayOfWeek(long dayOfWeek, String[] nameList) {
        return nameFromNumber(dayOfWeek + 1, nameList);
    }

    public String nameFromMonth(long month, String[] nameList) {
        return nameFromNumber(month, nameList);
    }

}
