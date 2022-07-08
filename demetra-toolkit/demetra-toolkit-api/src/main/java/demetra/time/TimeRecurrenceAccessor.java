package demetra.time;

public interface TimeRecurrenceAccessor {

    TimeInterval<?, ?> getInterval();

    int length();
}
