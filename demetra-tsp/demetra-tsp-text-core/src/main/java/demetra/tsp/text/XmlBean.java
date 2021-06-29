package demetra.tsp.text;

import demetra.tsprovider.FileBean;

import java.io.File;
import java.nio.charset.Charset;

@lombok.Data
public final class XmlBean implements FileBean {

    private File file;

    private Charset charset;
}
