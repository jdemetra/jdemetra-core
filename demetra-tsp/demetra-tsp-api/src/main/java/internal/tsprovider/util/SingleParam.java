package internal.tsprovider.util;

import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.Param;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

@lombok.AllArgsConstructor
public final class SingleParam<S extends IConfig, P> implements Param<S, P> {

    @lombok.NonNull
    private final P defaultValue;

    @lombok.NonNull
    private final String key;

    @lombok.NonNull
    private final Parser<P> parser;

    @lombok.NonNull
    private final Formatter<P> formatter;

    private boolean isValid(@NonNull String tmp) {
        return !tmp.isEmpty();
    }

    @Override
    public P defaultValue() {
        return defaultValue;
    }

    @Override
    public P get(IConfig config) {
        String tmp = config.get(key);
        if (tmp != null && isValid(tmp)) {
            P result = parser.parse(tmp);
            if (result != null) {
                return result;
            }
        }
        return defaultValue;
    }

    @Override
    public void set(IConfig.Builder<?, S> builder, P value) {
        Objects.requireNonNull(builder);
        if (!defaultValue.equals(value) && value != null) {
            String valueAsString = formatter.formatAsString(value);
            if (valueAsString != null) {
                builder.put(key, valueAsString);
            }
        }
    }
}
