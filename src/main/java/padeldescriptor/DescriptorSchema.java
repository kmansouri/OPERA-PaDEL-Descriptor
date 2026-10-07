package padeldescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import libpadeldescriptor.CDK_Descriptor;
import libpadeldescriptor.CDK_Fingerprint;
import libpadeldescriptor.CDK_FingerprintCount;
import libpadeldescriptor.libPaDELDescriptorType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Immutable descriptor selection and output schema. */
final class DescriptorSchema {
    final Set<String> activeTypes;
    final List<String> names;
    final List<Family> families;
    final String sha256;
    final String sourceDescription;

    static final class Family {
        final String kind;
        final String implementation;
        final int offset;
        final int length;

        Family(String kind, String implementation, int offset, int length) {
            this.kind = kind;
            this.implementation = implementation;
            this.offset = offset;
            this.length = length;
        }
    }

    static final class Bundle {
        final List<CDK_Descriptor> descriptors = new ArrayList<CDK_Descriptor>();
        final List<CDK_Fingerprint> fingerprints = new ArrayList<CDK_Fingerprint>();
        final List<CDK_FingerprintCount> fingerprintCounts = new ArrayList<CDK_FingerprintCount>();
    }

    private DescriptorSchema(Set<String> activeTypes, List<String> names,
                             List<Family> families, String sha256,
                             String sourceDescription) {
        this.activeTypes = Collections.unmodifiableSet(new LinkedHashSet<String>(activeTypes));
        this.names = Collections.unmodifiableList(new ArrayList<String>(names));
        this.families = Collections.unmodifiableList(new ArrayList<Family>(families));
        this.sha256 = sha256;
        this.sourceDescription = sourceDescription;
    }

    static DescriptorSchema load(CliOptions options) throws Exception {
        byte[] xml;
        String description;
        if (options.descriptorTypesPath != null) {
            File file = new File(options.descriptorTypesPath);
            xml = java.nio.file.Files.readAllBytes(file.toPath());
            description = file.getAbsolutePath();
        } else {
            InputStream in = DescriptorSchema.class.getResourceAsStream("/META-INF/descriptors.xml");
            if (in == null) throw new IOException("Embedded descriptor configuration is missing");
            try {
                xml = readAll(in);
            } finally {
                in.close();
            }
            description = "embedded META-INF/descriptors.xml";
        }

        Set<String> active = parseActiveTypes(xml, options.compute2D,
                options.compute3D, options.computeFingerprints);
        if (active.isEmpty()) {
            throw new IllegalArgumentException("No active descriptor families were selected");
        }

        Bundle bundle = createBundle(active);
        List<String> names = new ArrayList<String>();
        List<Family> families = new ArrayList<Family>();
        int offset = 0;
        for (CDK_Descriptor descriptor : bundle.descriptors) {
            String[] familyNames = descriptor.getDescriptorNames();
            Collections.addAll(names, familyNames);
            families.add(new Family("descriptor", descriptor.getClass().getName(),
                    offset, familyNames.length));
            offset += familyNames.length;
        }
        for (CDK_Fingerprint fingerprint : bundle.fingerprints) {
            String[] familyNames = fingerprint.getDescriptorNames();
            Collections.addAll(names, familyNames);
            families.add(new Family("fingerprint", fingerprint.getClass().getName(),
                    offset, familyNames.length));
            offset += familyNames.length;
        }
        for (CDK_FingerprintCount count : bundle.fingerprintCounts) {
            String[] familyNames = count.getDescriptorNames();
            Collections.addAll(names, familyNames);
            families.add(new Family("fingerprint-count", count.getClass().getName(),
                    offset, familyNames.length));
            offset += familyNames.length;
        }
        names = disambiguateNames(names, families);
        return new DescriptorSchema(active, names, families, hex(sha256(xml)), description);
    }

    Bundle newBundle() {
        return createBundle(activeTypes);
    }

    private static Bundle createBundle(Set<String> active) {
        Bundle bundle = new Bundle();
        libPaDELDescriptorType.SetDescriptorTypes(active, null, bundle.descriptors,
                bundle.fingerprints, bundle.fingerprintCounts);
        return bundle;
    }

    private static Set<String> parseActiveTypes(byte[] xml, boolean compute2D,
                                                boolean compute3D,
                                                boolean computeFingerprints)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        setFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        try { factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); } catch (Exception ignored) { }
        try { factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); } catch (Exception ignored) { }

        org.w3c.dom.Document doc = factory.newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(xml));
        Set<String> active = new LinkedHashSet<String>();
        NodeList groups = doc.getDocumentElement().getElementsByTagName("Group");
        for (int i = 0; i < groups.getLength(); i++) {
            Node node = groups.item(i);
            if (!(node instanceof Element)) continue;
            Element group = (Element) node;
            String type = group.getAttribute("name");
            boolean selected = ("2D".equals(type) && compute2D)
                    || ("3D".equals(type) && compute3D)
                    || ("Fingerprint".equals(type) && computeFingerprints);
            if (!selected) continue;
            NodeList descriptors = group.getElementsByTagName("Descriptor");
            for (int j = 0; j < descriptors.getLength(); j++) {
                Node item = descriptors.item(j);
                if (!(item instanceof Element)) continue;
                Element element = (Element) item;
                if (Boolean.parseBoolean(element.getAttribute("value"))) {
                    String name = element.getAttribute("name").trim();
                    if (!name.isEmpty()) active.add(name);
                }
            }
        }
        return active;
    }

    private static List<String> disambiguateNames(List<String> rawNames, List<Family> families) {
        java.util.Map<String, Integer> totals = new java.util.HashMap<String, Integer>();
        for (String name : rawNames) {
            Integer count = totals.get(name);
            totals.put(name, count == null ? 1 : count + 1);
        }
        List<String> result = new ArrayList<String>(rawNames.size());
        Set<String> used = new HashSet<String>();
        int familyIndex = 0;
        Family family = families.isEmpty() ? null : families.get(0);
        for (int i = 0; i < rawNames.size(); i++) {
            while (family != null && i >= family.offset + family.length) {
                familyIndex++;
                family = familyIndex < families.size() ? families.get(familyIndex) : null;
            }
            String raw = rawNames.get(i);
            String candidate = raw;
            Integer total = totals.get(raw);
            if (total != null && total.intValue() > 1) {
                candidate = raw + "__" + simpleName(family == null ? "descriptor" : family.implementation);
            }
            String base = candidate;
            int suffix = 2;
            while (!used.add(candidate)) candidate = base + "_" + suffix++;
            result.add(candidate);
        }
        return result;
    }

    private static String simpleName(String className) {
        int dot = className.lastIndexOf('.');
        return dot < 0 ? className : className.substring(dot + 1);
    }

    private static void setFeature(DocumentBuilderFactory factory, String name, boolean value) {
        try { factory.setFeature(name, value); } catch (Exception ignored) { }
    }

    private static byte[] readAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) >= 0) out.write(buffer, 0, n);
        return out.toByteArray();
    }

    static byte[] sha256(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(bytes);
    }

    static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) out.append(String.format(java.util.Locale.ROOT, "%02x", b & 0xff));
        return out.toString();
    }
}
