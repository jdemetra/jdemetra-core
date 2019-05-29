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
package ec.tstoolkit.utilities;

import com.google.common.io.Closer;
import ioutil.IO;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;

/**
 * Provides utility methods for the {@link Closeable} class and for related
 * classes.
 *
 * @author Philippe Charles
 */
@Deprecated
public final class Closeables {

    private Closeables() {
        // static class
    }

    @Deprecated
    public static void closeBoth(@Nonnull Closeable first, @Nonnull Closeable second) throws IOException {
        IO.closeBoth(first, second);
    }

    @Deprecated
    public static Closeable asCloseable(final XMLStreamWriter o) {
        return () -> {
            try {
                o.close();
            } catch (XMLStreamException ex) {
                throw new IOException("While closing XMLStreamWriter", ex);
            }
        };
    }

    @Deprecated
    public static <X extends XMLStreamWriter> X register(Closer closer, X o) {
        closer.register(asCloseable(o));
        return o;
    }

    @Deprecated
    public static Closeable asCloseable(final ResultSet o) {
        return () -> {
            try {
                o.close();
            } catch (SQLException ex) {
                throw new IOException("While closing ResultSet", ex);
            }
        };
    }

    @Deprecated
    public static <X extends ResultSet> X register(Closer closer, X o) {
        closer.register(asCloseable(o));
        return o;
    }

    @Deprecated
    public static Closeable asCloseable(final Statement o) {
        return () -> {
            try {
                o.close();
            } catch (SQLException ex) {
                throw new IOException("While closing Statement", ex);
            }
        };
    }

    @Deprecated
    public static <X extends Statement> X register(Closer closer, X o) {
        closer.register(asCloseable(o));
        return o;
    }

    @Deprecated
    public static Closeable asCloseable(final Connection o) {
        return () -> {
            try {
                o.close();
            } catch (SQLException ex) {
                throw new IOException("While closing Connection", ex);
            }
        };
    }

    @Deprecated
    public static <X extends Connection> X register(Closer closer, X o) {
        closer.register(asCloseable(o));
        return o;
    }

    @Deprecated
    public static void closeQuietly(Logger logger, Closeable... closeables) {
        for (Closeable o : closeables) {
            if (o != null) {
                try {
                    o.close();
                } catch (IOException ex) {
                    logger.error("While closing", ex);
                }
            }
        }
    }

    @Deprecated
    public static void closeQuietly(Logger logger, XMLStreamWriter... closeables) {
        for (XMLStreamWriter o : closeables) {
            if (o != null) {
                try {
                    o.close();
                } catch (XMLStreamException ex) {
                    logger.error("While closing XMLStream", ex);
                }
            }
        }
    }

    @Deprecated
    public static void closeQuietly(Logger logger, ResultSet... closeables) {
        for (ResultSet o : closeables) {
            if (o != null) {
                try {
                    o.close();
                } catch (SQLException ex) {
                    logger.error("While closing ResultSet", ex);
                }
            }
        }
    }

    @Deprecated
    public static void closeQuietly(Logger logger, Statement... closeables) {
        for (Statement o : closeables) {
            if (o != null) {
                try {
                    o.close();
                } catch (SQLException ex) {
                    logger.error("While closing Statement", ex);
                }
            }
        }
    }

    @Deprecated
    public static void closeQuietly(Logger logger, Connection... closeables) {
        for (Connection o : closeables) {
            if (o != null) {
                try {
                    o.close();
                } catch (SQLException ex) {
                    logger.error("While closing Connection", ex);
                }
            }
        }
    }
}
