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
package ec.jbench;

import com.google.common.base.Strings;
import ec.benchmarking.simplets.TsMultiBenchmarking;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.NamedObject;
import ec.tstoolkit.utilities.WeightedItem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class App {

    private static double rho_ = 1, lambda_ = 1;
    private static String dataFile_ = "test.csv";
    private static String tFile_ = null;
    private static String dFile_ = null;
    private static String cFile_ = null;
    private static String oFile_ = "bench.csv";
    private static TsMultiBenchmarking bench_ = new TsMultiBenchmarking();
    private static final char delim = ((DecimalFormat) DecimalFormat.getInstance()).getDecimalFormatSymbols().getDecimalSeparator();
    private static String sep = delim == ',' ? ";" : ",";

    static boolean read() {
        ArrayList<NamedObject<TsData>> s = read(dataFile_);
        if (s == null) {
            return false;
        } else {
            for (NamedObject<TsData> cur : s) {
                bench_.addInput(cur.name, cur.object);
            }
            return true;
        }
    }

    static ArrayList<NamedObject<TsData>> read(String file) {
        try {
            ArrayList<NamedObject<TsData>> ts = new ArrayList<>();
            InputStream stream = Files.newInputStream(Paths.get(file));
            DataInputStream in = new DataInputStream(stream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            NumberFormat fmt = DecimalFormat.getInstance();

            String sLine;
            while ((sLine = br.readLine()) != null) {
                String[] sArray = sLine.split(sep);
                if (sArray.length <= 3) {
                    continue;
                }
                String name = sArray[0].trim();
                int freq = Integer.parseInt(sArray[1]);
                int year = Integer.parseInt(sArray[2]);
                int period = Integer.parseInt(sArray[3]);
                int n = Integer.parseInt(sArray[4]);
                if (n > sArray.length - 5) {
                    n = sArray.length - 5;
                }
                double[] data = new double[n];
                for (int i = 0; i < n; ++i) {
                    data[i] = fmt.parse(sArray[i + 5]).doubleValue();
                }
                TsData s = new TsData(TsFrequency.valueOf(freq), year, period - 1, data, false);
                ts.add(new NamedObject<>(name, s));
            }
            stream.close();
            return ts;
        } catch (IOException | NumberFormatException | ParseException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    static boolean readCFile() {
        if (cFile_ == null) {
            return true;
        }
        try {

            InputStream stream = Files.newInputStream(Paths.get(cFile_));
            DataInputStream in = new DataInputStream(stream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            NumberFormat fmt = DecimalFormat.getInstance();

            String sLine;
            while ((sLine = br.readLine()) != null) {
                String[] sArray = sLine.split(sep);
                if (sArray.length % 2 != 0) {
                    continue;
                }

                String name = sArray[0].trim();
                String scnt = sArray[0];
                double dcnt = 0;
                if (!Strings.isNullOrEmpty(sArray[1])) {
                    dcnt = fmt.parse(sArray[1]).doubleValue();
                }
                TsMultiBenchmarking.ContemporaneousConstraintDescriptor cd;
                if (!Strings.isNullOrEmpty(scnt)) {
                    cd = new TsMultiBenchmarking.ContemporaneousConstraintDescriptor(scnt);
                } else {
                    cd = new TsMultiBenchmarking.ContemporaneousConstraintDescriptor(dcnt);
                }

                for (int i = 2; i < sArray.length; ++i) {
                    String s = sArray[i++].trim();
                    double c = 1;
                    if (!Strings.isNullOrEmpty(sArray[i])) {
                        c = fmt.parse(sArray[i]).doubleValue();
                    }
                    cd.components.add(new WeightedItem<>(s, c));
                }

                if (!bench_.addContemporaneousConstraint(cd)) {
                    System.out.println("Invalid constraint: " + name);
                    return false;
                }
            }
            stream.close();
            return true;
        } catch (IOException | ParseException err) {
            System.out.println(err.getMessage());
            return false;
        }
    }

    static boolean readTFile() {
        if (tFile_ == null) {
            return true;
        }
        try {
            InputStream stream = Files.newInputStream(Paths.get(tFile_));
            DataInputStream in = new DataInputStream(stream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String sLine;
            while ((sLine = br.readLine()) != null) {
                String[] sArray = sLine.split(sep);
                if (sArray.length != 2) {
                    continue;
                }
                String aname = sArray[0].trim();
                String dname = sArray[1].trim();

                if (!bench_.addTemporalConstraint(aname, dname)) {
                    System.out.println("Invalid constraint: " + aname);
                    return false;
                }
            }
            stream.close();
            return true;
        } catch (Exception err) {
            System.out.println(err.getMessage());
            return false;
        }
    }

    static boolean readDFile() {
        if (dFile_ == null) {
            return true;
        }
        ArrayList<NamedObject<TsData>> s = read(dFile_);
        if (s == null) {
            return false;
        } else {
            for (NamedObject<TsData> ns : s) {
                String an = "A_" + ns.name;
                bench_.addInput(an, ns.object);
                bench_.addTemporalConstraint(an, ns.name);
            }
            return true;
        }
    }

    static void write() throws IOException {
        BufferedWriter bw = null;
        try {
            bw = Files.newBufferedWriter(Paths.get(oFile_));
            NumberFormat fmt = DecimalFormat.getInstance();
            fmt.setMaximumFractionDigits(9);
            fmt.setMaximumIntegerDigits(50);
            fmt.setGroupingUsed(false);

            for (String n : bench_.endogenous()) {
                TsData s = bench_.getResult(n);
                bw.append(n);
                bw.append(sep);
                bw.append(Integer.toString(s.getFrequency().intValue()));
                bw.append(sep);
                bw.append(Integer.toString(s.getStart().getYear()));
                bw.append(sep);
                bw.append(Integer.toString(s.getStart().getPosition() + 1));
                bw.append(sep);
                bw.append(Integer.toString(s.getLength()));
                bw.append(sep);
                bw.append(fmt.format(s.get(0)));
                for (int j = 1; j < s.getLength(); ++j) {
                    bw.append(sep);
                    bw.append(fmt.format(s.get(j)));
                }
                bw.newLine();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (bw != null) {
                bw.flush();
                bw.close();
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (!decode(args)) {
                return;
            }

            bench_.setRho(rho_);
            bench_.setLambda(lambda_);

            System.out.println("loading data from " + dataFile_);
            if (!read()) {
                return;
            }
            if (!readCFile()) {
                return;
            }
            if (!readTFile()) {
                return;
            }

            if (!readDFile()) {
                return;
            }
            System.out.println("start processing... ");
            if (bench_.process()) {
                System.out.println("writing results in " + oFile_);
                write();
            }
        } catch (Exception err) {
            System.out.println(err.getMessage());
        }
    }

    private static boolean decode(String[] args) {
        try {
            int cur = 0;
            while (cur < args.length) {
                String cmd = args[cur++];
                if (cmd.length() == 0) {
                    return false;
                }
                if (cmd.equals("-i")) {
                    if (cur == args.length) {
                        return false;
                    }
                    dataFile_ = args[cur++];
                } else if (cmd.equals("-c")) {
                    if (cur == args.length) {
                        return false;
                    }
                    cFile_ = args[cur++];
                } else if (cmd.equals("-t")) {
                    if (cur == args.length) {
                        return false;
                    }
                    tFile_ = args[cur++];
                } else if (cmd.equals("-d")) {
                    if (cur == args.length) {
                        return false;
                    }
                    dFile_ = args[cur++];
                } else if (cmd.equals("-o")) {
                    if (cur == args.length) {
                        return false;
                    }
                    oFile_ = args[cur++];
                } else if (cmd.equals("-r")) {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    rho_ = Double.parseDouble(str);
                } else if (cmd.equals("-l")) {
                    if (cur == args.length) {
                        return false;
                    }
                    String str = args[cur++];
                    if (str.length() == 0 || str.charAt(0) == '-') {
                        return false;
                    }
                    lambda_ = Double.parseDouble(str);
                } else {
                    System.out.println("Unexpected argument");
                    return false;
                }
            }
            return true;
        } catch (Exception err) {
            System.out.println(err.getMessage());
            return false;
        }
    }
}