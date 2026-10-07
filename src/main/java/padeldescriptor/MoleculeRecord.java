package padeldescriptor;

import org.openscience.cdk.interfaces.IAtomContainer;

/** One input molecule and its stable sequence metadata. */
final class MoleculeRecord {
    final long index;
    final String name;
    final String sourceFile;
    final IAtomContainer molecule;
    final Throwable readError;

    MoleculeRecord(long index, String name, String sourceFile,
                   IAtomContainer molecule, Throwable readError) {
        this.index = index;
        this.name = name;
        this.sourceFile = sourceFile;
        this.molecule = molecule;
        this.readError = readError;
    }
}
