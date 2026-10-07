package padeldescriptor;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** RFC-4180-style UTF-8 CSV output with optional file splitting. */
final class CsvOutput implements Closeable {
    private final File requestedFile;
    private final List<String> names;
    private final int maxRowsPerFile;
    private PrintWriter writer;
    private long totalRows;
    private int rowsInCurrentFile;
    private int fileNumber;
    private File currentFile;

    CsvOutput(File requestedFile, List<String> names, int maxRowsPerFile) throws IOException {
        this.requestedFile = requestedFile.getAbsoluteFile();
        this.names = names;
        this.maxRowsPerFile = maxRowsPerFile;
        openNext();
    }

    synchronized void write(CalculationResult result) throws IOException {
        if (maxRowsPerFile > 0 && rowsInCurrentFile >= maxRowsPerFile) openNext();
        writeCell(result.name);
        for (String value : result.values) {
            writer.print(',');
            writeCell(value);
        }
        writer.println();
        if (writer.checkError()) throw new IOException("Could not write CSV file: " + currentFile);
        rowsInCurrentFile++;
        totalRows++;
    }

    long getTotalRows() { return totalRows; }
    int getFileCount() { return fileNumber; }
    File getCurrentFile() { return currentFile; }

    private void openNext() throws IOException {
        if (writer != null) writer.close();
        fileNumber++;
        rowsInCurrentFile = 0;
        currentFile = maxRowsPerFile > 0 ? numberedFile(requestedFile, fileNumber) : requestedFile;
        File parent = currentFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Cannot create output directory: " + parent);
        }
        writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(currentFile), StandardCharsets.UTF_8), 65536));
        writeCell("Name");
        for (String name : names) {
            writer.print(',');
            writeCell(name);
        }
        writer.println();
        if (writer.checkError()) throw new IOException("Could not create CSV file: " + currentFile);
    }

    private void writeCell(String value) {
        String text = value == null ? "" : value;
        boolean quote = text.indexOf(',') >= 0 || text.indexOf('"') >= 0
                || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0
                || (!text.isEmpty() && (Character.isWhitespace(text.charAt(0))
                || Character.isWhitespace(text.charAt(text.length() - 1))));
        if (!quote) {
            writer.print(text);
            return;
        }
        writer.print('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') writer.print("\"\"");
            else writer.print(c);
        }
        writer.print('"');
    }

    private static File numberedFile(File original, int number) {
        String name = original.getName();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        return new File(original.getParentFile(), base + "_" + number + ext);
    }

    @Override
    public synchronized void close() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }
}
