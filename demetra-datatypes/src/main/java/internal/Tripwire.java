package internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see java.util.Tripwire
 */
@lombok.experimental.UtilityClass
public class Tripwire {

    public final String TRIPWIRE_PROPERTY = "org.openjdk.java.util.stream.tripwire";

    /**
     * Should debugging checks be enabled?
     */
    public boolean ENABLED = AccessController.doPrivileged(
            (PrivilegedAction<Boolean>) () -> Boolean.getBoolean(TRIPWIRE_PROPERTY));

    /**
     * Produces a log warning, using {@code Logger.getLogger(className)}, using
     * the supplied message. The class name of {@code trippingClass} will be
     * used as the first parameter to the message.
     *
     * @param trippingClass Name of the class generating the message
     * @param msg A message format string of the type expected by {@link Logger}
     */
    public void trip(Class<?> trippingClass, String msg) {
        Logger.getLogger(trippingClass.getName()).log(Level.WARNING, msg, trippingClass.getName());
    }
}
