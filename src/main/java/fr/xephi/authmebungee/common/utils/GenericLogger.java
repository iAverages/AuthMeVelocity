package fr.xephi.authmebungee.common.utils;

import java.util.function.Supplier;

public interface GenericLogger {

    void info(String message);

    void warning(String message);

    void fine(String message);

    void debug(String message);

    void debug(String message, Object param1);

    void debug(String message, Object param1, Object param2);

    void debug(String message, Object... params);

    void debug(Supplier<String> msgSupplier);
}
