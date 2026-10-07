package padeldescriptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

/** Streaming readers for the file formats most commonly used by PaDEL. */
final class MoleculeReaders {
    interface Consumer {
        void accept(MoleculeRecord record) throws Exception;
    }

    private MoleculeReaders() {
    }

    static long read(File input, boolean useFilenameAsName, Consumer consumer, RunLog log)
            throws Exception {
        if (!input.exists()) throw new IOException("Input does not exist: " + input);
        List<File> files = new ArrayList<File>();
        if (input.isDirectory()) {
            File[] listed = input.listFiles();
            if (listed != null) {
                Arrays.sort(listed, new Comparator<File>() {
                    @Override public int compare(File a, File b) {
                        return a.getName().compareToIgnoreCase(b.getName());
                    }
                });
                for (File file : listed) if (file.isFile() && !file.isHidden()) files.add(file);
            }
        } else {
            files.add(input);
        }
        if (files.isEmpty()) throw new IOException("No input files found in: " + input);

        AtomicLong globalIndex = new AtomicLong();
        for (File file : files) {
            String extension = extension(file.getName());
            if (isSmiles(extension)) {
                readSmiles(file, useFilenameAsName, globalIndex, consumer, log);
            } else if ("sdf".equals(extension) || "sd".equals(extension) || "mol".equals(extension)) {
                readSdf(file, useFilenameAsName, globalIndex, consumer, log);
            } else {
                readGeneric(file, useFilenameAsName, globalIndex, consumer, log);
            }
        }
        return globalIndex.get();
    }

    private static void readSmiles(File file, boolean useFilenameAsName,
                                   AtomicLong globalIndex, Consumer consumer, RunLog log)
            throws Exception {
        SmilesParser parser = new SmilesParser(SilentChemObjectBuilder.getInstance());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8));
        try {
            String line;
            int fileIndex = 0;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue;
                fileIndex++;
                long index = globalIndex.incrementAndGet();
                String[] fields = trimmed.split("\\s+", 2);
                String name = fields.length > 1 && !fields[1].trim().isEmpty()
                        ? fields[1].trim() : generatedName(file, fileIndex, useFilenameAsName);
                try {
                    IAtomContainer molecule = parser.parseSmiles(fields[0]);
                    molecule.setProperty(CDKConstants.TITLE, name);
                    consumer.accept(new MoleculeRecord(index, name, file.getName(), molecule, null));
                } catch (Throwable ex) {
                    log.warn("Could not parse SMILES at " + file.getName() + ":" + fileIndex
                            + " (" + RunLog.concise(ex) + ")");
                    consumer.accept(new MoleculeRecord(index, name, file.getName(), null, ex));
                }
            }
        } finally {
            reader.close();
        }
    }

    private static void readSdf(File file, boolean useFilenameAsName,
                                AtomicLong globalIndex, Consumer consumer, RunLog log)
            throws Exception {
        InputStream in = new FileInputStream(file);
        IteratingSDFReader reader = new IteratingSDFReader(in, SilentChemObjectBuilder.getInstance(), true);
        int fileIndex = 0;
        try {
            while (reader.hasNext()) {
                fileIndex++;
                long index = globalIndex.incrementAndGet();
                try {
                    IAtomContainer molecule = reader.next();
                    String title = molecule.getProperty(CDKConstants.TITLE);
                    String name = title == null || title.trim().isEmpty()
                            ? generatedName(file, fileIndex, useFilenameAsName) : title.trim();
                    molecule.setProperty(CDKConstants.TITLE, name);
                    consumer.accept(new MoleculeRecord(index, name, file.getName(), molecule, null));
                } catch (Throwable ex) {
                    String name = generatedName(file, fileIndex, useFilenameAsName);
                    log.warn("Could not read molecule " + fileIndex + " from " + file.getName()
                            + " (" + RunLog.concise(ex) + ")");
                    consumer.accept(new MoleculeRecord(index, name, file.getName(), null, ex));
                }
            }
        } finally {
            reader.close();
        }
    }

    private static void readGeneric(File file, boolean useFilenameAsName,
                                    AtomicLong globalIndex, Consumer consumer, RunLog log)
            throws Exception {
        InputStream in = new FileInputStream(file);
        ISimpleChemObjectReader reader = null;
        try {
            reader = new ReaderFactory().createReader(in);
            if (reader == null) throw new IOException("Unsupported structure format: " + file.getName());
            IChemFile chemFile = SilentChemObjectBuilder.getInstance().newInstance(IChemFile.class);
            reader.read(chemFile);
            List<IAtomContainer> molecules = ChemFileManipulator.getAllAtomContainers(chemFile);
            int fileIndex = 0;
            for (IAtomContainer molecule : molecules) {
                fileIndex++;
                long index = globalIndex.incrementAndGet();
                String title = molecule.getProperty(CDKConstants.TITLE);
                String name = title == null || title.trim().isEmpty()
                        ? generatedName(file, fileIndex, useFilenameAsName) : title.trim();
                molecule.setProperty(CDKConstants.TITLE, name);
                consumer.accept(new MoleculeRecord(index, name, file.getName(), molecule, null));
            }
        } catch (Throwable ex) {
            log.warn("Could not read " + file.getName() + " (" + RunLog.concise(ex) + ")");
            long index = globalIndex.incrementAndGet();
            consumer.accept(new MoleculeRecord(index, generatedName(file, 1, useFilenameAsName),
                    file.getName(), null, ex));
        } finally {
            if (reader != null) reader.close();
            else in.close();
        }
    }

    private static boolean isSmiles(String extension) {
        return "smi".equals(extension) || "smiles".equals(extension)
                || "can".equals(extension) || "ismi".equals(extension)
                || "txt".equals(extension);
    }

    private static String generatedName(File file, int index, boolean useFilenameAsName) {
        String base = stripExtension(file.getName()).replaceAll("[^A-Za-z0-9._-]+", "_");
        if (useFilenameAsName) return index == 1 ? base : base + "_" + index;
        return "AUTOGEN_" + base + "_" + index;
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot <= 0 ? name : name.substring(0, dot);
    }
}
