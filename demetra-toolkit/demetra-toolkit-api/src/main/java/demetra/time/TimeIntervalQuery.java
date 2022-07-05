package demetra.time;

@FunctionalInterface
public interface TimeIntervalQuery<R> {

    R queryFrom(TimeIntervalAccessor timeInterval);
}
