package demetra.time;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

public interface TimeIntervalAccessor {

    Temporal start();

    Temporal end();

    TemporalAmount getDuration();
}
