package padeldescriptor;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Thread-safe console and optional file logger. */
final class RunLog implements Closeable {
    private final boolean quiet;
    private final PrintWriter writer;
    private int warningCount;
    private int errorCount;

    RunLog(boolean quiet, File logFile) throws IOException {
        this.quiet = quiet;
        if (logFile == null) {
            writer = null;
        } else {
            File parent = logFile.getAbsoluteFile().getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Cannot create log directory: " + parent);
            }
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(logFile), StandardCharsets.UTF_8)));
        }
    }

    synchronized void info(String message) {
        write("INFO", message, false);
    }

    synchronized void warn(String message) {
        warningCount++;
        write("WARN", message, true);
    }

    synchronized void error(String message) {
        errorCount++;
        write("ERROR", message, true);
    }

    synchronized void error(String message, Throwable cause) {
        error(message + ": " + concise(cause));
        if (writer != null && cause != null) {
            cause.printStackTrace(writer);
            writer.flush();
        }
    }

    synchronized int getWarningCount() {
        return warningCount;
    }

    synchronized int getErrorCount() {
        return errorCount;
    }

    private void write(String level, String message, boolean stderr) {
        String line = timestamp() + " " + level + " " + message;
        if (!quiet || stderr) {
            if (stderr) System.err.println(line);
            else System.out.println(line);
        }
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    private static String timestamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }

    static String concise(Throwable throwable) {
        if (throwable == null) return "unknown error";
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        String message = root.getMessage();
        return root.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }

    @Override
    public synchronized void close() {
        if (writer != null) writer.close();
    }
}
