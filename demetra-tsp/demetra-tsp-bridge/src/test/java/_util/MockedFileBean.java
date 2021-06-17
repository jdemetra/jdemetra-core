package _util;

import demetra.tsprovider.FileBean;

import java.io.File;

@lombok.Data
public final class MockedFileBean implements FileBean {

    File file;
}
