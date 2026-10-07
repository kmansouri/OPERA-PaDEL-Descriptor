package padeldescriptor;

import java.io.BufferedWriter;
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

/** JSON sidecar describing the engine, schema, options, and run outcome. */
final class MetadataWriter {
    private MetadataWriter() { }

    static void write(File output, CliOptions options, DescriptorSchema schema,
                      RunSummary summary, int warnings, int errors) throws IOException {
        File file = new File(output.getAbsolutePath() + ".metadata.json");
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)));
        try {
            out.println("{");
            field(out, "product", BuildInfo.PRODUCT, true);
            field(out, "version", BuildInfo.VERSION, true);
            field(out, "cdkVersion", BuildInfo.CDK_VERSION, true);
            field(out, "javaVersion", System.getProperty("java.version"), true);
            field(out, "generatedUtc", utcNow(), true);
            field(out, "input", new File(options.inputPath).getAbsolutePath(), true);
            field(out, "output", output.getAbsolutePath(), true);
            field(out, "descriptorConfiguration", schema.sourceDescription, true);
            field(out, "descriptorConfigurationSha256", schema.sha256, true);
            number(out, "descriptorColumns", schema.names.size(), true);
            number(out, "threads", options.resolvedThreads(), true);
            number(out, "waitingJobs", options.resolvedWaitingJobs(), true);
            number(out, "maxRuntimeMillis", options.maxRunTime, true);
            bool(out, "compute2D", options.compute2D, true);
            bool(out, "compute3D", options.compute3D, true);
            bool(out, "computeFingerprints", options.computeFingerprints, true);
            bool(out, "removeSalt", options.removeSalt, true);
            bool(out, "detectAromaticity", options.detectAromaticity, true);
            bool(out, "standardizeNitro", options.standardizeNitro, true);
            bool(out, "standardizeTautomers", options.standardizeTautomers, true);
            bool(out, "retain3D", options.retain3D, true);
            bool(out, "convert3D", options.convert3D, true);
            bool(out, "retainOrder", options.retainOrder, true);
            field(out, "aromaticityModel", options.aromaticityModel, true);
            field(out, "missingValue", options.missingValue, true);
            number(out, "inputMolecules", summary.inputMolecules, true);
            number(out, "outputMolecules", summary.outputMolecules, true);
            number(out, "successfulMolecules", summary.successfulMolecules, true);
            number(out, "failedMolecules", summary.failedMolecules, true);
            number(out, "timedOutMolecules", summary.timedOutMolecules, true);
            number(out, "readFailures", summary.readFailures, true);
            number(out, "standardizationFailures", summary.standardizationFailures, true);
            number(out, "descriptorFamilyErrors", summary.descriptorFamilyErrors, true);
            number(out, "missingValues", summary.missingValues, true);
            number(out, "warnings", warnings, true);
            number(out, "errors", errors, true);
            number(out, "elapsedMillis", summary.elapsedMillis(), false);
            out.println("}");
        } finally {
            out.close();
        }
    }

    private static void field(PrintWriter out, String key, String value, boolean comma) {
        out.print("  \""); out.print(escape(key)); out.print("\": \"");
        out.print(escape(value == null ? "" : value)); out.print('"');
        out.println(comma ? "," : "");
    }
    private static void number(PrintWriter out, String key, long value, boolean comma) {
        out.print("  \""); out.print(escape(key)); out.print("\": "); out.print(value);
        out.println(comma ? "," : "");
    }
    private static void bool(PrintWriter out, String key, boolean value, boolean comma) {
        out.print("  \""); out.print(escape(key)); out.print("\": "); out.print(value);
        out.println(comma ? "," : "");
    }
    private static String escape(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"': out.append("\\\""); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format(Locale.ROOT, "\\u%04x", (int)c));
                    else out.append(c);
            }
        }
        return out.toString();
    }
    private static String utcNow() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }
}
