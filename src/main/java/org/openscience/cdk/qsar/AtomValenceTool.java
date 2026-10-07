/*
 * Patched for PaDEL-Descriptor CDK2.
 *
 * Based on CDK's AtomValenceTool (LGPL-2.1-or-later).  CDK 2.13 still lazily
 * published a mutable HashMap before it had been populated, which allowed
 * parallel descriptor calculations to observe a partially initialized table.
 * This implementation constructs an immutable table during class
 * initialization, giving safe publication under the Java Memory Model.
 */
package org.openscience.cdk.qsar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openscience.cdk.interfaces.IAtom;

/** Returns the historical CDK valence used by several PaDEL descriptors. */
public final class AtomValenceTool {
    private static final Map<String, Integer> VALENCES = createValences();

    private AtomValenceTool() { }

    private static Map<String, Integer> createValences() {
        Map<String, Integer> values = new HashMap<String, Integer>();
        values.put("H", 1);
        values.put("He", 8);
        values.put("Ne", 8);
        values.put("Ar", 8);
        values.put("Kr", 8);
        values.put("Xe", 8);
        values.put("Hg", 2);
        values.put("Rn", 8);
        values.put("Li", 1);
        values.put("Be", 2);
        values.put("B", 3);
        values.put("C", 4);
        values.put("N", 5);
        values.put("O", 6);
        values.put("F", 7);
        values.put("Na", 1);
        values.put("Mg", 2);
        values.put("Al", 3);
        values.put("Si", 4);
        values.put("P", 5);
        values.put("S", 6);
        values.put("Cl", 7);
        values.put("K", 1);
        values.put("Ca", 2);
        values.put("Ga", 3);
        values.put("Ge", 4);
        values.put("As", 5);
        values.put("Se", 6);
        values.put("Br", 7);
        values.put("Rb", 1);
        values.put("Sr", 2);
        values.put("In", 3);
        values.put("Sn", 4);
        values.put("Sb", 5);
        values.put("Te", 6);
        values.put("I", 7);
        values.put("Cs", 1);
        values.put("Ba", 2);
        values.put("Tl", 3);
        values.put("Pb", 4);
        values.put("Bi", 5);
        values.put("Po", 6);
        values.put("At", 7);
        values.put("Fr", 1);
        values.put("Ra", 2);
        values.put("Cu", 2);
        values.put("Mn", 2);
        values.put("Co", 2);
        return Collections.unmodifiableMap(values);
    }

    public static int getValence(IAtom atom) {
        if (atom == null) throw new IllegalArgumentException("Atom must not be null");
        Integer valence = VALENCES.get(atom.getSymbol());
        if (valence == null) {
            throw new IllegalArgumentException("No historical valence is defined for element "
                    + atom.getSymbol());
        }
        return valence.intValue();
    }
}
