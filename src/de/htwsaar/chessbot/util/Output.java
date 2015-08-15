package de.htwsaar.chessbot.util;

public interface Output {
    /**
    * Line separator, OS-dependent (e.g.\ "\n" on UNIX)
    */
    String lineSeparator = System.getProperty("line.separator");

    /**
    * Print out the specified message.
    *
    * Specifically prints out what is returned by the message objects' <code>toString()</code> method.
    */
    void print(Object message);

    /**
    * Print a line separator.
    */
    void println();

    /**
    * Print out the specified message and add a line separator.
    *
    * @see Output.print
    */
    void println(Object message);

    /**
    * Print a formatted string.
    *
    * @see man printf
    */
    void printf(String format, Object ... args);
}