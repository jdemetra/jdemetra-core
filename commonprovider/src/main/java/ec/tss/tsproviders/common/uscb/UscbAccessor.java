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
package ec.tss.tsproviders.common.uscb;

import ec.tss.tsproviders.legacy.FileDataSourceId;
import ec.tss.tsproviders.utils.Parsers;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.Tokenizer;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kristof Bayens
 */
public class UscbAccessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UscbAccessor.class);
    private static final Charset USCB_CHARSET = Charset.defaultCharset();
    private final FileDataSourceId m_id;

    public UscbAccessor(FileDataSourceId id) {
        m_id = id;
    }

    public FileDataSourceId getId() {
        return m_id;
    }

    public TsData read(String uscbFolder) {
        Path file = Paths.get(uscbFolder, m_id.getFileName());
        TsData s = read_out(file);
        return s != null ? s : read_in(file);
    }

    private TsData read_out(Path file) {
        try (BufferedReader br = Files.newBufferedReader(file, USCB_CHARSET)) {
            String line = br.readLine();
            if (line == null) {
                return null;
            }
            Tokenizer tokenizer = new Tokenizer(line);
            if (!tokenizer.hasNextToken() || !tokenizer.nextToken().equals("date")) {
                return null;
            }
            String name = "";
            if (tokenizer.hasNextToken()) {
                name = tokenizer.nextToken();
            }
            List<USCensusObs> vals = new ArrayList<>();

            int maxfreq = 0;
            while ((line = br.readLine()) != null) {
                USCensusObs obs = new USCensusObs();
                if (obs.parse_Out(line)) {
                    vals.add(obs);
                }
                if (obs.period > maxfreq) {
                    maxfreq = obs.period;
                }
            }
            return create(vals, maxfreq);
        } catch (Exception ex) {
            LOGGER.error("While reading data", ex);
            return null;
        }
    }

    private TsData read_in(Path file) {
        try (BufferedReader br = Files.newBufferedReader(file, USCB_CHARSET)) {
            List<USCensusObs> vals = new ArrayList<>();

            int maxfreq = 0;
            String line;
            while ((line = br.readLine()) != null) {
                USCensusObs obs = new USCensusObs();
                if (obs.parse_In(line)) {
                    vals.add(obs);
                }
                if (obs.period > maxfreq) {
                    maxfreq = obs.period;
                }
            }
            return create(vals, maxfreq);
        } catch (Exception ex) {
            LOGGER.error("While reading data", ex);
            return null;
        }
    }

    private TsData create(List<USCensusObs> vals, int maxfreq) {
        if (vals.isEmpty()) {
            return null;
        }

        TsPeriod start = new TsPeriod(TsFrequency.valueOf(maxfreq), vals.get(0).year, vals.get(0).period - 1);
        TsPeriod cur = new TsPeriod(TsFrequency.valueOf(maxfreq));
        int lastval = vals.size() - 1;
        cur.set(vals.get(lastval).year, vals.get(lastval).period - 1);
        double[] data = new double[cur.minus(start) + 1];
        for (int i = 0; i < data.length; ++i) {
            data[i] = Double.NaN;
        }
        for (USCensusObs obs : vals) {
            cur.set(obs.year, obs.period - 1);
            int pos = cur.minus(start);
            data[pos] = obs.obs;
        }

        TsData series = new TsData(start, data, false);
        return series;
    }

    static class USCensusObs {

        static final double ND = -99999;
        static final Parsers.Parser<Double> VALUE_PARSER = Parsers.doubleParser();
        int year;
        int period;
        double obs;

        boolean parse_Out(String line) {
            try {
                String date = null, val = null;
                Tokenizer tokenizer = new Tokenizer(line);
                if (tokenizer.hasNextToken()) {
                    date = tokenizer.nextToken();
                }
                if (tokenizer.hasNextToken()) {
                    val = tokenizer.nextToken();
                }
                if (date != null && val != null) {
                    year = Integer.parseInt(date.substring(0, 4));
                    period = Integer.parseInt(date.substring(4, 6));
                    obs = VALUE_PARSER.tryParse(val).or(ND);
                    if (obs == ND) {
                        obs = Double.NaN;
                    }
                }
                return year > 0 && period > 0;
            } catch (NumberFormatException | NullPointerException ex) {
                LOGGER.error("While parsing data", ex);
                return false;
            }
        }

        boolean parse_In(String line) {
            try {
                Tokenizer tokenizer = new Tokenizer(line);
                if (tokenizer.hasNextToken()) {
                    year = Integer.parseInt(tokenizer.nextToken());
                }
                if (tokenizer.hasNextToken()) {
                    period = Integer.parseInt(tokenizer.nextToken());
                }
                if (tokenizer.hasNextToken()) {
                    obs = VALUE_PARSER.tryParse(tokenizer.nextToken()).or(Double.NaN);
                }
                return year > 0 && period > 0 && !tokenizer.hasNextToken();
            } catch (NumberFormatException | NullPointerException ex) {
                LOGGER.error("While parsing data", ex);
                return false;
            }
        }
    }
}
