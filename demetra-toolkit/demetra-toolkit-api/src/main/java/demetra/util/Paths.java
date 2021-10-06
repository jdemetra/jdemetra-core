/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
 */
package demetra.util;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Palate Jean
 */
@lombok.experimental.UtilityClass
public final class Paths {

    public final String DEMETRA = "Demetra+", DEF_FILE = "demetra";
    public final String DEF_FOLDER = new File(getDefaultHome(), DEMETRA).getPath();

    /**
     *
     * @param file
     * @param next
     * @return
     */
    public String changeExtension(String file, String next) {
        int ipos = file.length() - 1;
        while (ipos >= 0 && file.charAt(ipos) != '.'
                && file.charAt(ipos) != java.io.File.separatorChar
                && file.charAt(ipos) != java.io.File.pathSeparatorChar) {
            --ipos;
        }
        String sfile = null;
        if (ipos < 0 || file.charAt(ipos) != '.') {
            sfile = file;
        } else {
            sfile = file.substring(0, ipos);
        }
        if (next == null) {
            return sfile;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(sfile);
        builder.append('.');
        builder.append(next);
        return builder.toString();
    }

    public String addExtension(String file, String ext) {
        StringBuilder builder = new StringBuilder();
        builder.append(file);
        builder.append('.');
        builder.append(ext);
        return builder.toString();
    }

    /**
     *
     * @param file
     * @return
     */
    public String[] splitFile(String file) {
        String[] rslt = new String[3];
        int ipos = file.length() - 1;
        while (ipos >= 0 && file.charAt(ipos) != '.'
                && file.charAt(ipos) != java.io.File.separatorChar) {
            --ipos;
        }
        if (ipos > 0 && file.charAt(ipos) == '.') {
            rslt[2] = file.substring(ipos + 1);
        }
        if (ipos < 0) {
            rslt[1] = file;
            return rslt;
        }
        int epos = ipos--;
        while (ipos >= 0 && file.charAt(ipos) != java.io.File.separatorChar) {
            --ipos;
        }
        if (ipos > 0) {
            rslt[0] = file.substring(0, ipos);
            if (ipos + 1 < epos) {
                rslt[1] = file.substring(ipos + 1, epos);
            } else {
                rslt[1] = rslt[2];
                rslt[2] = null;
            }
        } else {
            rslt[1] = file.substring(0, epos);
        }
        return rslt;
    }

    public String getBaseName(String file) {
        String[] s = splitFile(file);
        return s[1];
    }

    public String getFullPath(String file) {
        try {
            File tmp = new File(file);
            return splitFile(tmp.getCanonicalPath())[0];
        } catch (IOException ex) {
            return null;
        }
    }

    public String concatenate(String root, String folder) {
        StringBuilder builder = new StringBuilder();
        if (root != null) {
            builder.append(root);
            if (root.charAt(root.length() - 1) != java.io.File.separatorChar
                    && folder != null) {
                builder.append(java.io.File.separatorChar);
            }
        }
        if (folder != null) {
            builder.append(folder);
        }
        return builder.toString();
    }

    public String getDefaultHome() {
        String parent = System.getenv("HOMESHARE");
        if (parent != null) {
            return parent;
        }
        parent = System.getenv("USERPROFILE");
        if (parent != null) {
            return parent;
        } else {
            return ".";
        }
    }

    private String fileFromId(Id id) {
        int n = id.getCount();
        switch (n) {
            case 0:
                return DEF_FILE;
            case 1:
                return id.get(0);
            default:
                String result = concatenate(id.get(0), id.get(1));
                for (int i = 2; i < n; ++i) {
                    result = concatenate(result, id.get(i));
                }
                return result;
        }
    }

    private String fileFromId(String folder, Id id) {
        return concatenate(folder, fileFromId(id));
    }

    public String fileFromContext(String folder, Object context) {
        if (context == null) {
            return folder(folder);
        }
        if (context instanceof Id) {
            Id id = (Id) context;
            return fileFromId(folder(folder), (Id) context);
        } else {
            return concatenate(folder(folder), context.toString());
        }
    }

    public String folderFromContext(String folder, Object context) {
        String nfolder = folder(folder);
        if (context != null && context instanceof Id) {
            Id parent = (Id) context;
            for (int i = 0; i < parent.getCount(); ++i) {
                nfolder = concatenate(nfolder, parent.get(i));
            }
        }
        File Folder = new File(nfolder);
        if (!Folder.exists()) {
            Folder.mkdirs();
        }
        return nfolder;
    }

    public File folderFromContext(File folder, Object context) {
        File nfolder = folder(folder);
        if (context != null && context instanceof Id) {
            Id parent = (Id) context;
            for (int i = 0; i < parent.getCount(); ++i) {
                nfolder = new File(nfolder, parent.get(i));
            }
        }
        if (!nfolder.exists()) {
            nfolder.mkdirs();
        }
        return nfolder;
    }

    public String folder(String folder) {
        if (folder == null || folder.length() == 0) {
            return DEF_FOLDER;
        } else {
            return folder;
        }
    }

    public File folder(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return new File(DEF_FOLDER);
        } else {
            return folder;
        }
    }

}
