package padeldescriptor;

/** Aggregate counters for a descriptor run. */
final class RunSummary {
    long inputMolecules;
    long outputMolecules;
    long successfulMolecules;
    long failedMolecules;
    long timedOutMolecules;
    long readFailures;
    long standardizationFailures;
    long descriptorFamilyErrors;
    long missingValues;
    long startMillis = System.currentTimeMillis();
    long endMillis;

    void add(CalculationResult result) {
        outputMolecules++;
        descriptorFamilyErrors += result.calculationErrors;
        missingValues += result.missingValues;
        if (result.timedOut) timedOutMolecules++;
        if (result.readFailed) readFailures++;
        if (result.standardizationFailed) standardizationFailures++;
        if (result.failed()) failedMolecules++;
        else successfulMolecules++;
    }

    long elapsedMillis() {
        long end = endMillis == 0 ? System.currentTimeMillis() : endMillis;
        return Math.max(0L, end - startMillis);
    }
}
