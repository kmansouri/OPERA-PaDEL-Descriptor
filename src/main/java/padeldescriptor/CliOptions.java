package padeldescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/** Parsed command-line and configuration options. */
final class CliOptions {
    String inputPath;
    String outputPath;
    String descriptorTypesPath;
    String configPath;
    String tautomerListPath;
    String forceField = "mm2";
    String missingValue = "";
    String aromaticityModel = "daylight";
    String listSchemaPath;

    boolean compute2D;
    boolean compute3D;
    boolean computeFingerprints;
    boolean removeSalt;
    boolean detectAromaticity;
    boolean standardizeTautomers;
    boolean standardizeNitro;
    boolean retain3D;
    boolean convert3D;
    boolean logResults;
    boolean retainOrder;
    boolean useFilenameAsMolName;
    boolean strict;
    boolean quiet;
    boolean help;
    boolean version;

    int maxThreads = -1;
    int maxJobsWaiting = -1;
    int maxCompoundsPerFile;
    long maxRunTime = -1L;

    private static final Set<String> VALUE_OPTIONS = new HashSet<String>(Arrays.asList(
            "config", "descriptortypes", "dir", "file", "tautomerlist",
            "threads", "waitingjobs", "maxruntime", "maxcpdperfile",
            "forcefield", "missingvalue", "aromaticitymodel", "listschema"));

    static CliOptions parse(String[] args) throws IOException {
        CliOptions options = new CliOptions();
        List<String> tokens = new ArrayList<String>(Arrays.asList(args));

        // Load the legacy properties file first; explicit CLI options override it.
        String config = findOptionValue(tokens, "config");
        if (config != null) {
            options.configPath = config;
            options.loadConfig(new File(config));
        }

        for (int i = 0; i < tokens.size(); i++) {
            String raw = tokens.get(i);
            if (!raw.startsWith("-")) {
                throw new IllegalArgumentException("Unexpected argument: " + raw);
            }
            String keyValue = stripDashes(raw);
            String key;
            String inlineValue = null;
            int eq = keyValue.indexOf('=');
            if (eq >= 0) {
                key = normalizeKey(keyValue.substring(0, eq));
                inlineValue = keyValue.substring(eq + 1);
            } else {
                key = normalizeKey(keyValue);
            }

            if (VALUE_OPTIONS.contains(key)) {
                String value = inlineValue;
                if (value == null) {
                    if (i + 1 >= tokens.size()) {
                        throw new IllegalArgumentException("Missing value for -" + key);
                    }
                    value = tokens.get(++i);
                }
                options.applyValue(key, value);
            } else {
                options.applyFlag(key);
            }
        }
        options.validate();
        return options;
    }

    int resolvedThreads() {
        int available = Math.max(1, Runtime.getRuntime().availableProcessors());
        if (maxThreads <= 0) {
            return available;
        }
        return Math.max(1, maxThreads);
    }

    int resolvedWaitingJobs() {
        if (maxJobsWaiting > 0) {
            return maxJobsWaiting;
        }
        // Bounded and memory-conscious default while keeping workers supplied.
        return Math.max(4, resolvedThreads() * 4);
    }

    private void validate() {
        if (help || version) {
            return;
        }
        if (listSchemaPath != null) {
            if (!compute2D && !compute3D && !computeFingerprints) {
                compute2D = true;
            }
            return;
        }
        if (inputPath == null || inputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required -dir input file or directory");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required -file output CSV");
        }
        if (!compute2D && !compute3D && !computeFingerprints) {
            throw new IllegalArgumentException("Select at least one of -2d, -3d, or -fingerprints");
        }
        if (maxThreads == 0 || maxThreads < -1) {
            throw new IllegalArgumentException("-threads must be -1 or a positive integer");
        }
        if (maxJobsWaiting == 0 || maxJobsWaiting < -1) {
            throw new IllegalArgumentException("-waitingjobs must be -1 or a positive integer");
        }
        if (maxRunTime == 0 || maxRunTime < -1) {
            throw new IllegalArgumentException("-maxruntime must be -1 or a positive number of milliseconds");
        }
        if (maxCompoundsPerFile < 0) {
            throw new IllegalArgumentException("-maxcpdperfile must be 0 or a positive integer");
        }
        if (!("daylight".equals(aromaticityModel) || "cdklegacy".equals(aromaticityModel))) {
            throw new IllegalArgumentException("-aromaticitymodel must be daylight or cdklegacy");
        }
    }

    private void applyFlag(String key) {
        if ("2d".equals(key)) compute2D = true;
        else if ("3d".equals(key)) compute3D = true;
        else if ("fingerprints".equals(key)) computeFingerprints = true;
        else if ("removesalt".equals(key)) removeSalt = true;
        else if ("detectaromaticity".equals(key)) detectAromaticity = true;
        else if ("standardizetautomers".equals(key)) standardizeTautomers = true;
        else if ("standardizenitro".equals(key)) standardizeNitro = true;
        else if ("retain3d".equals(key)) retain3D = true;
        else if ("convert3d".equals(key)) convert3D = true;
        else if ("log".equals(key)) logResults = true;
        else if ("retainorder".equals(key)) retainOrder = true;
        else if ("noretainorder".equals(key)) retainOrder = false;
        else if ("usefilenameasmolname".equals(key)) useFilenameAsMolName = true;
        else if ("strict".equals(key)) strict = true;
        else if ("quiet".equals(key)) quiet = true;
        else if ("help".equals(key) || "h".equals(key)) help = true;
        else if ("version".equals(key) || "v".equals(key)) version = true;
        else throw new IllegalArgumentException("Unknown option: -" + key);
    }

    private void applyValue(String key, String value) {
        if ("config".equals(key)) configPath = value;
        else if ("descriptortypes".equals(key)) descriptorTypesPath = value;
        else if ("dir".equals(key)) inputPath = value;
        else if ("file".equals(key)) outputPath = value;
        else if ("tautomerlist".equals(key)) tautomerListPath = value;
        else if ("threads".equals(key)) maxThreads = parseInt(key, value);
        else if ("waitingjobs".equals(key)) maxJobsWaiting = parseInt(key, value);
        else if ("maxruntime".equals(key)) maxRunTime = parseLong(key, value);
        else if ("maxcpdperfile".equals(key)) maxCompoundsPerFile = parseInt(key, value);
        else if ("forcefield".equals(key)) forceField = value.toLowerCase(Locale.ROOT);
        else if ("missingvalue".equals(key)) missingValue = value;
        else if ("aromaticitymodel".equals(key)) aromaticityModel = value.toLowerCase(Locale.ROOT);
        else if ("listschema".equals(key)) listSchemaPath = value;
        else throw new IllegalArgumentException("Unknown option: -" + key);
    }

    private void loadConfig(File file) throws IOException {
        Properties p = new Properties();
        InputStream in = new FileInputStream(file);
        try {
            p.load(in);
        } finally {
            in.close();
        }
        inputPath = p.getProperty("Directory", inputPath);
        outputPath = p.getProperty("DescriptorFile", outputPath);
        descriptorTypesPath = p.getProperty("DescriptorTypesFile", descriptorTypesPath);
        compute2D = bool(p, "Compute2D", compute2D);
        compute3D = bool(p, "Compute3D", compute3D);
        computeFingerprints = bool(p, "ComputeFingerprints", computeFingerprints);
        removeSalt = bool(p, "RemoveSalt", removeSalt);
        detectAromaticity = bool(p, "DetectAromaticity", detectAromaticity);
        standardizeTautomers = bool(p, "StandardizeTautomers", standardizeTautomers);
        standardizeNitro = bool(p, "StandardizeNitro", standardizeNitro);
        retain3D = bool(p, "Retain3D", retain3D);
        convert3D = bool(p, "Convert3D", convert3D);
        logResults = bool(p, "Log", logResults);
        retainOrder = bool(p, "RetainOrder", retainOrder);
        useFilenameAsMolName = bool(p, "UseFilenameAsMolName", useFilenameAsMolName);
        strict = bool(p, "Strict", strict);
        tautomerListPath = p.getProperty("TautomerFile", tautomerListPath);
        missingValue = p.getProperty("MissingValue", missingValue);
        aromaticityModel = p.getProperty("AromaticityModel", aromaticityModel).toLowerCase(Locale.ROOT);
        maxThreads = integer(p, "MaxThreads", maxThreads);
        maxJobsWaiting = integer(p, "MaxJobsWaiting", maxJobsWaiting);
        maxRunTime = longValue(p, "MaxRunTime", maxRunTime);
        maxCompoundsPerFile = integer(p, "MaxCpdPerFile", maxCompoundsPerFile);
    }

    private static String findOptionValue(List<String> args, String wanted) {
        for (int i = 0; i < args.size(); i++) {
            String token = args.get(i);
            if (!token.startsWith("-")) continue;
            String stripped = stripDashes(token);
            int eq = stripped.indexOf('=');
            String key = normalizeKey(eq >= 0 ? stripped.substring(0, eq) : stripped);
            if (!wanted.equals(key)) continue;
            if (eq >= 0) return stripped.substring(eq + 1);
            if (i + 1 < args.size()) return args.get(i + 1);
        }
        return null;
    }

    private static String stripDashes(String value) {
        int i = 0;
        while (i < value.length() && value.charAt(i) == '-') i++;
        return value.substring(i);
    }

    private static String normalizeKey(String key) {
        String k = key.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
        if ("input".equals(k) || "directory".equals(k)) return "dir";
        if ("output".equals(k) || "descriptorfile".equals(k)) return "file";
        if ("maxthreads".equals(k)) return "threads";
        if ("maxjobswaiting".equals(k)) return "waitingjobs";
        return k;
    }

    private static boolean bool(Properties p, String key, boolean fallback) {
        String v = p.getProperty(key);
        return v == null ? fallback : Boolean.parseBoolean(v.trim());
    }

    private static int integer(Properties p, String key, int fallback) {
        String v = p.getProperty(key);
        return v == null ? fallback : parseInt(key, v);
    }

    private static long longValue(Properties p, String key, long fallback) {
        String v = p.getProperty(key);
        return v == null ? fallback : parseLong(key, v);
    }

    private static int parseInt(String key, String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer for -" + key + ": " + value);
        }
    }

    private static long parseLong(String key, String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer for -" + key + ": " + value);
        }
    }
}
