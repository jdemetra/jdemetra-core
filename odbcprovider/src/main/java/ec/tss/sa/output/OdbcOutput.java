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


package ec.tss.sa.output;

import ec.satoolkit.DecompositionMode;
import ec.satoolkit.ISaSpecification;
import ec.tss.TsIdentifier;
import ec.tss.TsMoniker;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.IOutput;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kristof Bayens
 */
public class OdbcOutput implements IOutput<SaDocument<ISaSpecification>> {
    public static final Logger LOGGER = LoggerFactory.getLogger(OdbcOutputFactory.class);

    private final static String g_SelectSeries = "SELECT * FROM Series WHERE Processing=? AND Source=? AND Identifier=?";
    private final static String g_InsertSeries = "INSERT INTO Series (Processing, Source, Identifier, Description) VALUES (?,?,?,?)";
    private final static String g_DeleteSeries = "DELETE FROM Series WHERE Processing=? AND Source=? AND Identifier=?";
    private final static String g_DeleteProcessing = "DELETE FROM Series WHERE Processing=?";
    private final static String g_DeleteModel = "DELETE FROM Models WHERE SeriesID=?";
    private final static String g_DeleteObservations = "DELETE FROM Observations WHERE SeriesID=?";
    private final static String g_DeleteAllObservations = "DELETE FROM Observations WHERE SeriesID IN (SELECT ID FROM Series WHERE Processing=? )";
    private final static String g_DeleteAllModels = "DELETE FROM Models WHERE SeriesID IN (SELECT ID FROM Series WHERE Processing=? )";
    private final static String g_InsertObs = "INSERT INTO Observations (SeriesID, ObsPeriod, ObsValue, ObsType) VALUES (?,?,?,?)";
    private final static String g_InsertModel = "INSERT INTO Models (SeriesID, DecompositionType, XmlModel) VALUES (?,?,?)";

    private PreparedStatement selectseriesCmd_;
    private PreparedStatement insertseriesCmd_;
    private PreparedStatement deletemodelCmd_;
    private PreparedStatement deleteseriesCmd_;
    private PreparedStatement deleteprocessingCmd_;
    private PreparedStatement deleteobservationsCmd_;
    private PreparedStatement deleteallobservationsCmd_;
    private PreparedStatement deleteallmodelsCmd_;
    private PreparedStatement insertmodelCmd_;
    private PreparedStatement insertobsCmd_;

    private String dsn_ = "SAResults";
    private boolean orig_ = true, sa_ = true, seas_ = true, trend_ = true, irr_ = true, cal_ = true, model_ = true;
    private Connection connection_ = null;
    private String processing_;

    public OdbcOutput(OdbcSaOutputConfiguration config) {
        model_ = config.isSaveModel();
        orig_ = config.isSaveOriginal();
        sa_ = config.isSaveSa();
        seas_ = config.isSaveSeas();
        irr_ = config.isSaveIrregular();
        cal_ = config.isSaveCalendar();
        setConnection(config.getDSN());
    }

    public void closeConnection() throws SQLException {
        close();
        connection_ = null;
    }

    public void setConnection(String dsn) {
        dsn_ = dsn;
    }
    
    private void open() throws Exception {
        if (connection_ == null) {
            try {
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");                
            }
            catch (Exception ex) {
                LOGGER.error("Can't load Sun's odbc driver");
                throw ex;
            }
            try {
                String url = getConnectionString();
                connection_ = DriverManager.getConnection(url);
            }
            catch (SQLException ex) {
                LOGGER.error("Can't open connection");
                throw ex;
            }
        }
    }
    
    private void close() throws SQLException {
            connection_.close();
    }

    protected String getConnectionString() {
        return String.format("jdbc:odbc:%s", dsn_);
    }

    public void clear(TsMoniker id) {
        try {
            deleteSeries(id);
        }
        catch(SQLException ex) {}
    }

    private short convert(DecompositionMode type) {
        switch(type) {
            case Additive:
                return (short)0;
            default:
                return (short)1;
        }
    }

    private void saveModel(int id, DecompositionMode type, String spec) throws Exception {
        // delete previous results
        open();
        try {
            insertModel(id, convert(type), spec);
        }
        catch(SQLException ex) {}
    }
    
    private void process(SaSummary summary) throws Exception {
        int id = getId(summary.Identifier);
        if (id < 0)
            return;
        try {
            deleteModel(id);
            deleteObservations(id);
            if (model_)
                saveModel(id, summary.Mode, summary.Spec);
            if (orig_)
                insertTimeSeries(id, summary.Orig, (short)0);
            if (trend_)
                insertTimeSeries(id, summary.T, (short)1);
            if (sa_)
                insertTimeSeries(id, summary.SA, (short)2);
            if (seas_)
                insertTimeSeries(id, summary.S, (short)3);
            if (irr_)
                insertTimeSeries(id, summary.I, (short)5);
            if (cal_)
                insertTimeSeries(id, summary.CAL, (short)4);
        }
        catch(SQLException ex) {
            
        }
        LOGGER.debug(summary.Identifier.getName());
    }

    public void delete(String name) throws Exception {
        open();
        deleteprocessingCmd_ = initDeleteProcessing();
        deleteallobservationsCmd_ = initDeleteAllObservations();
        deleteallmodelsCmd_ = initDeleteAllModels();
        try {
            deleteAllObservations(name);
            deleteAllModels(name);
            deleteProcessing(name);
            connection_.close();
        }
        catch(SQLException ex) {

        }
    }

    @Override
    public void start(Object context) throws Exception {
        String file=context.toString();
        if (file != null)
            processing_ = file;
        if (processing_ == null || processing_.length() == 0)
            processing_ = "TEMP";
        open();
        selectseriesCmd_ = initSelectSeries();
        insertseriesCmd_ = initInsertSeries();
        deletemodelCmd_ = initDeleteModel();
        deleteobservationsCmd_ = initDeleteObservations();
        insertmodelCmd_ = initInsertModel();
        insertobsCmd_ = initInsertObservation();
        deleteseriesCmd_ = initDeleteSeries();
    }

    @Override
    public void process(SaDocument<ISaSpecification> doc) throws Exception {
        process(new SaSummary(doc));
    }

    @Override
    public void end(Object context) throws Exception {
        close();
        processing_ = null;
    }

    @Override
    public String getName() {
        return "ODBC";
    }

    @Override
    public boolean isAvailable() {
        try {
            open();
            boolean rslt = !connection_.isClosed();
            if (rslt)
                connection_.close();
            return rslt;
        }
        catch(Exception ex) {
            return false;
        }
    }

    private int getId(TsIdentifier series) throws Exception {
        open();
        // try to get the DataRow from the database        
        int id = -1;
        try {
            ResultSet table = selectSeries(series);
            if (table.next())
                id = table.getInt(1);
            else
                id = addId(series);
        }
        catch(SQLException ex) {}
        return id;
    }

    private int addId(TsIdentifier series) throws Exception {
        try {
            open();
            insertSeries(series);
        }
        catch (SQLException ex) {
            return -1;
        }
        return getId(series);
    }

    private PreparedStatement initSelectSeries() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_SelectSeries);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private ResultSet selectSeries(TsIdentifier id) throws SQLException {
        selectseriesCmd_.setString(1, processing_);
        selectseriesCmd_.setString(2, id.getMoniker().getSource());
        selectseriesCmd_.setString(3, id.getMoniker().getId());
        return selectseriesCmd_.executeQuery();
    }
    
    private PreparedStatement initInsertSeries() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_InsertSeries);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private void insertSeries(TsIdentifier id) throws SQLException {
        insertseriesCmd_.setString(1, processing_);
        insertseriesCmd_.setString(2, id.getMoniker().getSource());
        insertseriesCmd_.setString(3, id.getMoniker().getId());
        insertseriesCmd_.setString(4, id.getName());
        insertseriesCmd_.executeUpdate();
    }

    private PreparedStatement initDeleteModel() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_DeleteModel);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private PreparedStatement initDeleteObservations() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_DeleteObservations);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private PreparedStatement initDeleteSeries() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_DeleteSeries);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private PreparedStatement initDeleteProcessing() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_DeleteProcessing);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private PreparedStatement initDeleteAllObservations() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_DeleteAllObservations);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private PreparedStatement initDeleteAllModels() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_DeleteAllModels);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private void deleteSeries(TsMoniker id) throws SQLException {
        deleteseriesCmd_.setString(1, processing_);
        deleteseriesCmd_.setString(2, id.getSource());
        deleteseriesCmd_.setString(3, id.getId());
        deleteseriesCmd_.execute();
    }

    private void deleteProcessing(String name) throws SQLException {
        deleteprocessingCmd_.setString(1, name);
        deleteprocessingCmd_.execute();
    }

    private void deleteModel(int id) throws SQLException {
        deletemodelCmd_.setInt(1, id);
        deletemodelCmd_.execute();
    }

    private void deleteObservations(int id) throws SQLException {
        deleteobservationsCmd_.setInt(1, id);
        deleteobservationsCmd_.execute();
    }

    private void deleteAllModels(String name) throws SQLException {
        deleteallmodelsCmd_.setString(1, name);
        deleteallmodelsCmd_.execute();
    }

    private void deleteAllObservations(String name) throws SQLException {
        deleteallobservationsCmd_.setString(1, name);
        deleteallobservationsCmd_.execute();
    }

    private PreparedStatement initInsertModel() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_InsertModel);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private PreparedStatement initInsertObservation() {
        PreparedStatement command = null;
        try {
            command = connection_.prepareStatement(g_InsertObs);
        }
        catch(SQLException ex) {}
        finally {
            return command;
        }
    }

    private void insertModel(int id, short type, String p) throws SQLException {
        insertmodelCmd_.setInt(1, id);
        insertmodelCmd_.setShort(2, type);
        insertmodelCmd_.setString(3, p);
        insertmodelCmd_.execute();
    }

    private void insertObservation(int id, TsPeriod period, double val, short type) throws SQLException {
        insertobsCmd_.setInt(1, id);
        insertobsCmd_.setDate(2, new Date(period.lastday().getTime().getTime()));
        insertobsCmd_.setDouble(3, val);
        insertobsCmd_.setShort(4, type);
        insertobsCmd_.execute();
    }

    private void insertTimeSeries(int id, TsData s, short type) throws SQLException {
        if (s == null)
            return;
        TsPeriod period = s.getStart();
        for (int i = 0; i < s.getLength(); ++i, period = period.plus(1))
            insertObservation(id, period, s.get(i), type);
    }
}
