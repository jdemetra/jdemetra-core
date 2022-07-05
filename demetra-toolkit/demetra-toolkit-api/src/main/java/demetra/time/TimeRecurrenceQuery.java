package demetra.time;

@FunctionalInterface
public interface TimeRecurrenceQuery<R> {

    R queryFrom(TimeRecurrenceAccessor timeRecurrence);
}
