package demetra.tsp.text;

import demetra.timeseries.util.ObsGathering;
import demetra.tsprovider.FileBean;
import demetra.tsprovider.util.ObsFormat;

import java.io.File;
import java.nio.charset.Charset;

@lombok.Data
public final class TxtBean implements FileBean {

    public enum Delimiter {TAB, SEMICOLON, COMMA, SPACE}

    public enum TextQualifier {NONE, QUOTE, DOUBLE_QUOTE}

    File file;

    Charset charset;

    ObsFormat format;

    ObsGathering gathering;

    Delimiter delimiter;

    TextQualifier textQualifier;

    boolean headers;

    int skipLines;
}
