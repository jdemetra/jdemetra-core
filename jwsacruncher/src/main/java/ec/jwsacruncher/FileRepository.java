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
package ec.jwsacruncher;

import com.google.common.base.Throwables;
import ec.jwsacruncher.xml.XmlGenericWorkspace;
import ec.jwsacruncher.xml.XmlWksElement;
import ec.jwsacruncher.xml.XmlWorkspace;
import ec.jwsacruncher.xml.XmlWorkspaceItem;
import ec.satoolkit.GenericSaProcessingFactory;
import ec.tss.sa.SaProcessing;
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.sa.XmlSaProcessing;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.calendars.GregorianCalendarManager;
import ec.tstoolkit.timeseries.calendars.IGregorianCalendarProvider;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.utilities.LinearId;
import ec.tstoolkit.utilities.NameManager;
import ec.tstoolkit.utilities.Paths;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Jean Palate
 */
public class FileRepository {

    public static final String VAR = "Variables", CAL = "Calendars", SA = "SAProcessing", MULTIDOCUMENTS = "multi-documents";
    public static final String NAME = "File", FILENAME = "fileName", VERSION = "20120925";
    public static final LinearId ID = new LinearId(GenericSaProcessingFactory.FAMILY, MULTIDOCUMENTS),
            VID = new LinearId("Utilities", "Variables");
    private static final String SEP = "@";
    static final JAXBContext XML_GENERIC_WS_CONTEXT;
    static final JAXBContext XML_WS_CONTEXT;
    public static boolean legacy = false;
    public static String root;

    public static boolean isLegacy() {
        return legacy;
    }

    public static File getRootFolder() {
        return new File(root);
    }

    static {
        try {
            XML_GENERIC_WS_CONTEXT = JAXBContext.newInstance(XmlGenericWorkspace.class);
            XML_WS_CONTEXT = JAXBContext.newInstance(XmlWorkspace.class);
        } catch (JAXBException ex) {
            throw Throwables.propagate(ex);
        }
    }

    public static boolean loadCalendars(String file, boolean legacy) {
        GregorianCalendarManager activeMgr = ProcessingContext.getActiveContext().getGregorianCalendars();
        Class<? extends IXmlConverter<GregorianCalendarManager>> clazz = legacy 
                ? ec.tss.xml.legacy.XmlCalendars.class
                : ec.tss.xml.calendar.XmlCalendars.class;
        GregorianCalendarManager mgr = ItemRepository.loadLegacy(file, clazz);
        if (mgr != null) {
            for (String s : mgr.getNames()) {
                if (!activeMgr.contains(s)) {
                    IGregorianCalendarProvider cal = mgr.get(s);
                    activeMgr.set(s, cal);
                }
            }
            activeMgr.resetDirty();
            return true;
        } else {
            return false;
        }
    }

    public static boolean loadLegacyUserVariables(String vars, String file) {
        NameManager<TsVariables> activeMgr = ProcessingContext.getActiveContext().getTsVariableManagers();
        TsVariables mgr = ItemRepository.loadLegacy(file, ec.tss.xml.legacy.XmlTsVariables.class);
        if (mgr != null) {
            activeMgr.set(vars, mgr);
            activeMgr.resetDirty();
            return true;
        } else {
            return false;
        }
    }

    private static String getCalendarFile(File ws, String name, boolean create) {
        String folder = getRepositoryFolder(ws, CAL, create);
        return Paths.changeExtension(Paths.concatenate(folder, name), "xml");
    }

    private static String getVariablesFile(File ws, String name, boolean create) {
        String folder = getRepositoryFolder(ws, VAR, create);
        return Paths.changeExtension(Paths.concatenate(folder, name), "xml");
    }

    public static String getRepositoryFolder(File ws, String repository, boolean create) {
        File frepo = new File(root, repository);
        if (frepo.exists() && !frepo.isDirectory()) {
            return null;
        }
        if (!frepo.exists() && create) {
            frepo.mkdirs();
        }
        return frepo.getAbsolutePath();
    }

    public static String getRepositoryRootFolder(File id) {
        return Paths.changeExtension(id.getAbsolutePath(), null);
    }

    public static Map<String, SaProcessing> loadProcessing(File file) {
        root = getRepositoryRootFolder(file);
        Map<String, SaProcessing> sa = loadLegacy(file);
        if (sa != null) {
            return sa;
        } else {
            return load(file);
        }
    }

    private static Map<String, SaProcessing> loadLegacy(File file) {
        try {
            Unmarshaller unmarshaller = XML_WS_CONTEXT.createUnmarshaller();
            XmlWorkspace xws = (XmlWorkspace) unmarshaller.unmarshal(file);
            if (xws == null) {
                return null;
            }
            if (xws.saProcessing == null) {
                return null;
            }
            legacy = true;
            loadCalendars(getCalendarFile(file, CAL, false), true);
            loadLegacyUserVariables(ProcessingContext.LEGACY, getVariablesFile(file, VAR, false));
            LinkedHashMap<String, SaProcessing> sa = new LinkedHashMap<>();
            for (XmlWksElement el : xws.saProcessing) {
                String xfile = el.file;
                if (xfile == null) {
                    xfile = el.name;
                }
                String pfolder = getRepositoryFolder(file, SA, false);
                File pfile = new File(pfolder, xfile);
                SaProcessing p = loadProcessing(Paths.changeExtension(pfile.getAbsolutePath(), "xml"));
                if (p != null) {
                    sa.put(xfile, p);
                }
            }
            return sa;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void write(File file, String xfile, SaProcessing processing) {
        if (legacy) {
            writeLegacy(file, xfile, processing);
        } else {
            writeInfo(file, xfile, processing);
        }
    }

    public static void writeLegacy(File file, String xfile, SaProcessing processing) {
        try {
            String pfolder = getRepositoryFolder(file, SA, false);
            String nfile = Paths.changeExtension(xfile, "xml");
            String ofile = Paths.changeExtension(xfile, "bak");

            Path tfile = java.nio.file.Paths.get(pfolder, nfile);
            Path bfile = java.nio.file.Paths.get(pfolder, ofile);
            Files.copy(tfile, bfile);

            ItemRepository.saveLegacy(tfile.toAbsolutePath().toString(), processing, XmlSaProcessing.class);
        } catch (Exception ex) {
        }
    }

    public static void writeInfo(File file, String xfile, SaProcessing processing) {
        try {
            String pfolder = getRepositoryFolder(file, SA, false);
            String nfile = Paths.changeExtension(xfile, "xml");
            String ofile = Paths.changeExtension(xfile, "bak");

            Path tfile = java.nio.file.Paths.get(pfolder, nfile);
            Path bfile = java.nio.file.Paths.get(pfolder, ofile);
            Files.copy(tfile, bfile, StandardCopyOption.REPLACE_EXISTING);

            ItemRepository.saveInfo(tfile.toAbsolutePath().toString(), processing);
        } catch (Exception ex) {
        }
    }

    private static SaProcessing loadProcessing(String sfile) {
        SaProcessing p = ItemRepository.loadLegacy(sfile, XmlSaProcessing.class);
        if (p != null) {
            return p;
        }
        return ItemRepository.loadInfo(sfile, SaProcessing.class);

    }

    private static Map<String, SaProcessing> load(File file) {
        try {
            Unmarshaller unmarshaller = XML_GENERIC_WS_CONTEXT.createUnmarshaller();
            XmlGenericWorkspace xws = (XmlGenericWorkspace) unmarshaller.unmarshal(file);
            if (xws == null) {
                return null;
            }
            legacy = false;
            // load calendars (same as the legacy code)
            loadCalendars(getCalendarFile(file, CAL, false), false);
            // search for processing
            LinkedHashMap<String, SaProcessing> sa = new LinkedHashMap<>();
            for (XmlWorkspaceItem el : xws.items) {
                LinearId curid = new LinearId(el.family.split(SEP));
                if (curid.equals(ID)) {
                    String xfile = el.file;
                    if (xfile == null) {
                        xfile = el.name;
                    }
                    String pfolder = getRepositoryFolder(file, SA, false);
                    File pfile = new File(pfolder, xfile);
                    SaProcessing p = loadProcessing(Paths.changeExtension(pfile.getAbsolutePath(), "xml"));
                    if (p != null) {
                        sa.put(xfile, p);
                    }
                } else if (curid.equals(VID)) {
                    String xfile = el.file;
                    if (xfile == null) {
                        xfile = el.name;
                    }
                    String pfolder = getRepositoryFolder(file, VAR, false);
                    File pfile = new File(pfolder, xfile);
                    TsVariables v = loadVariables(Paths.changeExtension(pfile.getAbsolutePath(), "xml"));
                    if (v != null) {
                        ProcessingContext.getActiveContext().getTsVariableManagers().set(el.name, v);
                    }

                }
            }
            return sa;
        } catch (Exception ex) {
            return null;
        }
    }

    private static TsVariables loadVariables(String sfile) {
        TsVariables vars=ItemRepository.loadInfo(sfile, TsVariables.class);
        if (vars == null){
            vars=ItemRepository.loadLegacy(sfile, ec.tss.xml.regression.XmlTsVariables.class);
        }
        return vars;
    }
}
