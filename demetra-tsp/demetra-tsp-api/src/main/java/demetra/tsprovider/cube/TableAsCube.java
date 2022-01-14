package demetra.tsprovider.cube;

import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.util.ObsFormat;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class TableAsCube {

    @lombok.NonNull
    @lombok.Singular
    List<String> dimensions;

    @lombok.NonNull
    @lombok.Builder.Default
    String timeDimension = "";

    @lombok.NonNull
    @lombok.Builder.Default
    String measure = "";

    @lombok.NonNull
    @lombok.Builder.Default
    String version = "";

    @lombok.NonNull
    @lombok.Builder.Default
    String label = "";

    @lombok.NonNull
    @lombok.Builder.Default
    ObsFormat obsFormat = ObsFormat.DEFAULT;

    @lombok.NonNull
    @lombok.Builder.Default
    ObsGathering obsGathering = ObsGathering.DEFAULT;
}
