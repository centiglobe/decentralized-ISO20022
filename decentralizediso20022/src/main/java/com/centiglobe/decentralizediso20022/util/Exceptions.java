package com.centiglobe.decentralizediso20022.util;

/**
 * A class for various utility methods related to Exceptions
 * 
 * @author William Stackenäs
 */
public class Exceptions {

    /**
     * Obtains a cause {@link Throwable} of a given class of the given {@link Throwable}
     * 
     * @param e The {@link Throwable} to obt ain the root cause from
     * @param cause The class of the cause throwable to obtain
     * 
     * @return The {@link Throwable} cause of the given class if it exists,
     *         otherwise null
     */
    public static Throwable getCauseOfClass(Throwable e, Class<? extends Throwable> cause) {
        if (e == null || e.getClass() == cause)
            return e;
        return getCauseOfClass(e.getCause(), cause);
    }
}
