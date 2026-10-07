package padeldescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.aromaticity.Aromaticity;
import org.openscience.cdk.aromaticity.Kekulization;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.modeling.builder3d.ModelBuilder3D;
import org.openscience.cdk.modeling.builder3d.TemplateHandler3D;
import org.openscience.cdk.smirks.Smirks;
import org.openscience.cdk.smirks.SmirksTransform;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.periodictable.PeriodicTable;

/**
 * CDK 2 molecule preparation used before descriptor calculation.
 *
 * <p>The implementation uses public CDK APIs. Independent 1-D/2-D molecule
 * preparation is parallel; CDK 2.13 coordinate generation is serialized
 * because its cached builder and template handler contain mutable global
 * state.</p>
 */
final class MoleculeStandardizer {
    private static final String POLAR_NITRO =
            "[N+0:1](=[O+0:2])=[O+0:3]>>[N+:1](=[O+0:2])[O-:3]";

    /*
     * CDK 2.13 caches ModelBuilder3D and TemplateHandler3D in process-wide
     * singletons.  Both contain mutable state and their lazy template loading
     * is not thread-safe.  Concurrent coordinate generation can therefore
     * corrupt template parsing or mapping.  Serialize only the coordinate
     * generation phase; molecule-level descriptor calculation remains
     * parallel after each molecule has coordinates.
     */
    private static final Object MODEL_BUILDER_3D_LOCK = new Object();

    private final CliOptions options;
    private final List<String> tautomerTransforms;

    MoleculeStandardizer(CliOptions options, List<String> tautomerTransforms) {
        this.options = options;
        this.tautomerTransforms = tautomerTransforms == null
                ? Collections.<String>emptyList()
                : new ArrayList<String>(tautomerTransforms);
    }

    IAtomContainer standardize(IAtomContainer input) throws Exception {
        IAtomContainer molecule = standardizeBase(input);

        // PaDEL's historical 1-D/2-D pipeline used explicit hydrogens.
        configureAtoms(molecule);
        CDKHydrogenAdder.getInstance(molecule.getBuilder()).addImplicitHydrogens(molecule);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
        configureAtoms(molecule);
        Cycles.markRingAtomsAndBonds(molecule);

        if (options.convert3D) {
            molecule = generate3D(molecule);
        }
        return molecule;
    }

    /**
     * Prepare a coordinate-preserving molecule for 3-D descriptors.  Implicit
     * hydrogens are intentionally not materialized because newly created
     * hydrogen atoms have no coordinates.  The original heavy-atom geometry is
     * retained while chemical typing/aromaticity is refreshed.
     */
    IAtomContainer standardizeRetained3D(IAtomContainer input) throws Exception {
        IAtomContainer molecule = standardizeBase(input);
        for (IAtom atom : molecule.atoms()) {
            if (atom.getPoint3d() == null) {
                throw new CDKException("-retain3d requires 3-D coordinates for every retained atom; "
                        + "use -convert3d to generate coordinates");
            }
        }
        return molecule;
    }

    private IAtomContainer standardizeBase(IAtomContainer input) throws Exception {
        if (input == null) {
            throw new IllegalArgumentException("Molecule is null");
        }
        IAtomContainer molecule = (IAtomContainer) input.clone();
        if (options.removeSalt) {
            molecule = largestFragment(molecule);
        }

        cleanBasicProperties(molecule);
        configureAtoms(molecule);

        if (options.standardizeNitro) {
            normalizeNitro(molecule);
            configureAtoms(molecule);
        }

        if (options.standardizeTautomers && !tautomerTransforms.isEmpty()) {
            applyTautomerTransforms(molecule);
            configureAtoms(molecule);
        }

        if (options.detectAromaticity) {
            perceiveAromaticity(molecule);
        } else {
            Cycles.markRingAtomsAndBonds(molecule);
        }
        return molecule;
    }

    private static IAtomContainer largestFragment(IAtomContainer molecule) {
        if (molecule.getAtomCount() == 0 || ConnectivityChecker.isConnected(molecule)) {
            return molecule;
        }
        IAtomContainerSet parts = ConnectivityChecker.partitionIntoMolecules(molecule);
        IAtomContainer best = null;
        for (IAtomContainer part : parts.atomContainers()) {
            if (best == null || compareFragments(part, best) > 0) {
                best = part;
            }
        }
        return best == null ? molecule : best;
    }

    private static int compareFragments(IAtomContainer a, IAtomContainer b) {
        int c = Integer.compare(heavyAtoms(a), heavyAtoms(b));
        if (c != 0) return c;
        c = Integer.compare(a.getBondCount(), b.getBondCount());
        if (c != 0) return c;
        return Integer.compare(a.getAtomCount(), b.getAtomCount());
    }

    private static int heavyAtoms(IAtomContainer molecule) {
        int count = 0;
        for (IAtom atom : molecule.atoms()) {
            Integer number = atom.getAtomicNumber();
            if (number == null || number.intValue() != 1) count++;
        }
        return count;
    }

    private static void cleanBasicProperties(IAtomContainer molecule) {
        for (IAtom atom : molecule.atoms()) {
            if (atom.getAtomicNumber() == null || atom.getAtomicNumber().intValue() <= 0) {
                Integer number = PeriodicTable.getAtomicNumber(atom.getSymbol());
                if (number != null) atom.setAtomicNumber(number);
            }
            if (atom.getFormalCharge() == null) atom.setFormalCharge(0);
            if (atom.getAtomicNumber() != null
                    && atom.getAtomicNumber().intValue() == 7
                    && molecule.getConnectedBondsCount(atom) == 4
                    && atom.getFormalCharge().intValue() == 0) {
                atom.setFormalCharge(1);
            }
        }
    }

    private static void configureAtoms(IAtomContainer molecule) throws CDKException {
        AtomContainerManipulator.percieveAtomTypesAndConfigureUnsetProperties(molecule);
        CDKHydrogenAdder.getInstance(molecule.getBuilder()).addImplicitHydrogens(molecule);
    }

    private static void normalizeNitro(IAtomContainer molecule) throws CDKException {
        // Apply repeatedly in case a molecule contains more than one neutral NO2 group.
        for (int i = 0; i < 32; i++) {
            if (!Smirks.apply(molecule, POLAR_NITRO)) break;
        }
    }

    private void applyTautomerTransforms(IAtomContainer molecule) throws CDKException {
        List<SmirksTransform> transforms = new ArrayList<SmirksTransform>();
        for (String smirks : tautomerTransforms) {
            if (smirks == null || smirks.trim().isEmpty()) continue;
            SmirksTransform transform = Smirks.compile(smirks.trim());
            if (transform == null || transform.message() != null) {
                throw new CDKException("Invalid tautomer SMIRKS: " + smirks
                        + (transform == null ? "" : " (" + transform.message() + ")"));
            }
            transforms.add(transform);
        }
        int remaining = 100;
        boolean changed;
        do {
            changed = false;
            for (SmirksTransform transform : transforms) {
                if (transform.apply(molecule)) changed = true;
                if (--remaining <= 0) break;
            }
        } while (changed && remaining > 0);
    }

    private void perceiveAromaticity(IAtomContainer molecule) throws CDKException {
        // Kekulise existing aromatic input before clearing and reapplying a model.
        try {
            Kekulization.kekulize(molecule);
        } catch (CDKException ex) {
            // Some valid structures cannot be kekulised. The aromaticity model may
            // still handle them from the existing bond orders, so continue.
        }
        Aromaticity.clear(molecule);
        Cycles.markRingAtomsAndBonds(molecule);
        Aromaticity aromaticity;
        if ("cdklegacy".equals(options.aromaticityModel)) {
            aromaticity = new Aromaticity(Aromaticity.Model.CDK_AtomTypes,
                    Cycles.cdkAromaticSet());
        } else {
            aromaticity = new Aromaticity(Aromaticity.Model.Daylight,
                    Cycles.or(Cycles.all(), Cycles.vertexShort()));
        }
        aromaticity.apply(molecule);
        Cycles.markRingAtomsAndBonds(molecule);
    }

    private IAtomContainer generate3D(IAtomContainer molecule)
            throws CDKException, CloneNotSupportedException, IOException {
        synchronized (MODEL_BUILDER_3D_LOCK) {
            ModelBuilder3D builder;
            if (options.forceField != null && !options.forceField.trim().isEmpty()) {
                builder = ModelBuilder3D.getInstance(
                        TemplateHandler3D.getInstance(), options.forceField, molecule.getBuilder());
            } else {
                builder = ModelBuilder3D.getInstance(molecule.getBuilder());
            }
            return builder.generate3DCoordinates(molecule, true);
        }
    }
}
