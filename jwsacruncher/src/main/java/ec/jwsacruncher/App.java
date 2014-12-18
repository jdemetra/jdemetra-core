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

import ec.tss.ITsProvider;
import ec.tss.TsFactory;
import ec.tss.sa.EstimationPolicyType;
import ec.tss.sa.SaManager;
import ec.tss.sa.SaProcessing;
import ec.tss.sa.diagnostics.*;
import ec.tss.sa.output.BasicConfiguration;
import ec.tss.sa.output.CsvMatrixOutputConfiguration;
import ec.tss.sa.output.CsvMatrixOutputFactory;
import ec.tss.sa.output.CsvOutputConfiguration;
import ec.tss.sa.output.CsvOutputFactory;
import ec.tss.sa.processors.TramoSeatsProcessor;
import ec.tss.sa.processors.X13Processor;
import ec.tss.tsproviders.IFileLoader;
import ec.tss.tsproviders.TsProviders;
import ec.tss.tsproviders.common.random.RandomProvider;
import ec.tstoolkit.utilities.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Kristof Bayens
 */
public class App {

    static WsaConfig Config;
    static File file;
    //static String File = "My Documents\\Demetra+\\Workspace_1.xml";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        long T0 = System.currentTimeMillis();
        Config = new WsaConfig();
        if (!decodeArgs(args)) {
            System.out.println("Wrong arguments");
            return;
        }

        if (args == null || args.length == 0) {
            return;
        }
        if (file == null) {
            return;
        }
        File[] paths;
        if (Config.Paths != null) {
            paths = new File[Config.Paths.length];
            for (int i = 0; i < paths.length; ++i) {
                paths[i] = new File(Config.Paths[i]);
            }
        } else {
            paths = new File[0];
        }

        // providers
        TsFactory.instance.addAll(ServiceLoader.load(ITsProvider.class));
        TsFactory.instance.add(new RandomProvider());
        for (IFileLoader o : TsProviders.all().filter(IFileLoader.class)) {
            o.setPaths(paths);
        }

        // methods
        SaManager.instance.add(new TramoSeatsProcessor());
        SaManager.instance.add(new X13Processor());

        // diagnostics
        SaManager.instance.add(new CoherenceDiagnosticsFactory());
        SaManager.instance.add(new ResidualsDiagnosticsFactory());
        SaManager.instance.add(new OutOfSampleDiagnosticsFactory());
        SaManager.instance.add(new ResidualSeasonalityDiagnosticsFactory());
        SaManager.instance.add(new SpectralDiagnosticsFactory());
        SaManager.instance.add(new MDiagnosticsFactory());
        SaManager.instance.add(new SeatsDiagnosticsFactory());

        Map<String, SaProcessing> sa = FileRepository.loadProcessing(file);
        if (sa == null || sa.isEmpty()) {
            return;
        }
        if (App.Config.ndecs != null) {
            BasicConfiguration.setDecimalNumber(App.Config.ndecs);
        }
        if (App.Config.csvsep != null && App.Config.csvsep.length()==1) {
            BasicConfiguration.setCsvSeparator(App.Config.csvsep.charAt(0));
        }

        CsvOutputConfiguration csvconfig = new CsvOutputConfiguration();
        if (App.Config.Output == null) {
            App.Config.Output = Paths.concatenate(FileRepository.getRepositoryRootFolder(file), "Output");
        }
        File output = new File(App.Config.Output);
        if (!output.exists()) {
            output.mkdirs();
        }
        csvconfig.setFolder(new File(App.Config.Output));
        csvconfig.setPresentation(App.Config.getLayout());
        csvconfig.setSeries(Arrays.asList(App.Config.TSMatrix));
        CsvOutputFactory fac = new CsvOutputFactory(csvconfig);

        CsvMatrixOutputConfiguration mcsvconfig = new CsvMatrixOutputConfiguration();
        mcsvconfig.setFolder(new File(App.Config.Output));
        if (App.Config.Matrix != null) {
            mcsvconfig.setItems(Arrays.asList(App.Config.Matrix));
        }
        CsvMatrixOutputFactory mfac = new CsvMatrixOutputFactory(mcsvconfig);

        SaManager.instance.add(fac);
        SaManager.instance.add(mfac);
        for (Entry<String, SaProcessing> entry : sa.entrySet()) {
            SaProcessing processing = entry.getValue();
            long t0 = System.currentTimeMillis();
            System.out.println("Refreshing data");
            processing.refresh(Config.getPolicy(), false);
            SaBatchInformation info;
            if (processing.size() > Config.BundleSize) {
                info = new SaBatchInformation(Config.BundleSize);
            } else {
                info = new SaBatchInformation(0);
            }
            info.setName(entry.getKey());
            info.setItems(processing);
            SaBatchProcessor processor = new SaBatchProcessor(info, new ConsoleFeedback());
            processor.process();
            System.out.println("Saving new processing...");
            FileRepository.write(file, entry.getKey(), processing);
            System.out.print("Processing time: ");
            System.out.print(.001 * (System.currentTimeMillis() - t0));
            System.out.println();
        }

        long T1 = System.currentTimeMillis();
        System.out.print("Total processing time: ");
        System.out.print(.001 * (T1 - T0));
        System.out.println();
    }

    private static boolean decodeArgs(String[] args) {
        if (args == null || args.length == 0) {
            try {
                JAXBContext context = JAXBContext.newInstance(WsaConfig.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(App.Config, new FileWriter("wsacruncher.params"));
            } catch (JAXBException | IOException e) {
                System.err.println("Failed to create params file: " + e.getMessage());
            }
            return true;
        }
        //
        int cur = 0;
        while (cur < args.length) {
            String cmd = args[cur++];
            if (cmd.length() == 0) {
                return false;
            }
            if (cmd.charAt(0) != '-') {
                file = new File(cmd);
            } else {
                if (cmd.equals("-x") || cmd.equals("-X")) {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    try {
                        JAXBContext context = JAXBContext.newInstance(WsaConfig.class);
                        Unmarshaller unmarshaller = context.createUnmarshaller();
                        App.Config = (WsaConfig) unmarshaller.unmarshal(new FileReader(str));
                        return true;
                    } catch (JAXBException | FileNotFoundException e) {
                        System.out.print("Invalid configuration file");
                        return false;
                    }
                }
                if (cmd.equals("-d")) {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    Config.Output = str;
                } else if (cmd.equals("-m")) {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    try {
                        FileReader fr = new FileReader(str);
                        BufferedReader br = new BufferedReader(fr);
                        List<String> items = new ArrayList<>();
                        while (true) {
                            String line = br.readLine();
                            if (line == null) {
                                break;
                            }
                            items.add(line);
                        }
                        Config.Matrix = items.toArray(new String[items.size()]);
                    } catch (Exception e) {
                        return false;
                    }
                } else if (cmd.equals("-p")) {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    Config.policy = str;
                    if (Config.getPolicy() == EstimationPolicyType.None) {
                        return false;
                    }
                } else if (cmd.equals("-f")) {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    Config.layout = str;
                } else if (cmd.equals("-t")) {
                    // No longer supported
                    // App.Config.Diagnostics = true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
//    public static <T> T read(String file, Class<T> tclass) {
//        Object o = null;
//        try {
//            JAXBContext context = JAXBContext.newInstance(tclass);
//            Unmarshaller unmarshaller = context.createUnmarshaller();
//            o = unmarshaller.unmarshal(new FileReader(file));
//        }
//        catch(JAXBException ex) {
//
//        }
//        catch(IOException ex) {
//
//        }
//        return (T)o;
//    }
//
//    public static void write(String file, IXmlConverter t) {
//        try {
//            JAXBContext context = JAXBContext.newInstance(t.getClass());
//            Marshaller marshaller = context.createMarshaller();
//            marshaller.marshal(t, new FileWriter(file));
//        }
//        catch(JAXBException ex) {
//
//        }
//        catch(IOException ex) {
//
//        }
//    }
}
