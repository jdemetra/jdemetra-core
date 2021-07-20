package _util;

import ec.tss.tsproviders.IFileBean;

import java.io.File;

@lombok.Data
public final class MockedFileBeanV2 implements IFileBean {

    File file;
}
