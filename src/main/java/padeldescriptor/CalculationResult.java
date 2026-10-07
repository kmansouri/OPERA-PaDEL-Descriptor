package padeldescriptor;

/** Completed (or partially completed) result for one molecule. */
final class CalculationResult {
    final long index;
    final String name;
    final String sourceFile;
    final String[] values;
    final int calculationErrors;
    final int missingValues;
    final boolean timedOut;
    final boolean readFailed;
    final boolean standardizationFailed;
    final long elapsedMillis;
    final String summary;

    CalculationResult(long index, String name, String sourceFile, String[] values,
                      int calculationErrors, int missingValues, boolean timedOut,
                      boolean readFailed, boolean standardizationFailed,
                      long elapsedMillis, String summary) {
        this.index = index;
        this.name = name;
        this.sourceFile = sourceFile;
        this.values = values;
        this.calculationErrors = calculationErrors;
        this.missingValues = missingValues;
        this.timedOut = timedOut;
        this.readFailed = readFailed;
        this.standardizationFailed = standardizationFailed;
        this.elapsedMillis = elapsedMillis;
        this.summary = summary;
    }

    boolean failed() {
        return timedOut || readFailed || standardizationFailed || calculationErrors > 0;
    }
}
