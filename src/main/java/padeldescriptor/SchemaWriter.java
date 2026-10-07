package padeldescriptor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/** Writes the selected descriptor schema for model provenance. */
final class SchemaWriter {
    private SchemaWriter() { }

    static void write(File file, DescriptorSchema schema) throws IOException {
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Cannot create schema directory: " + parent);
        }
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)));
        try {
            out.println("Index,Name,FamilyKind,Implementation");
            int familyIndex = 0;
            DescriptorSchema.Family family = schema.families.isEmpty() ? null : schema.families.get(0);
            for (int i = 0; i < schema.names.size(); i++) {
                while (family != null && i >= family.offset + family.length) {
                    familyIndex++;
                    family = familyIndex < schema.families.size()
                            ? schema.families.get(familyIndex) : null;
                }
                out.print(i + 1);
                out.print(',');
                csv(out, schema.names.get(i));
                out.print(',');
                csv(out, family == null ? "" : family.kind);
                out.print(',');
                csv(out, family == null ? "" : family.implementation);
                out.println();
            }
        } finally {
            out.close();
        }
    }

    private static void csv(PrintWriter out, String value) {
        String text = value == null ? "" : value;
        out.print('"');
        out.print(text.replace("\"", "\"\""));
        out.print('"');
    }
}
