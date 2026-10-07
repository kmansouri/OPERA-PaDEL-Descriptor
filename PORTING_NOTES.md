# CDK 2.13 porting notes

## Scope

The port includes the historical PaDEL descriptor registry, descriptor adapters, PaDEL custom 1-D/2-D descriptors, 3-D descriptors, and fingerprints. The GUI was not carried forward; the product is a headless descriptor engine intended for OPERA and automated pipelines.

## Principal API migrations

- `IMolecule`/`IMoleculeSet` and legacy concrete molecule classes were replaced with `IAtomContainer`/`IAtomContainerSet` and builder-based construction.
- Removed graph index calls were replaced with `IAtomContainer.indexOf` and current connected-bond methods.
- Legacy atom/bond flags were replaced with `isAromatic`, `isInRing`, and their current setters.
- `SSSRFinder.findEssentialRings` was replaced with `Cycles.essential(...).toRingSet()`.
- Legacy SMARTS query objects were replaced by `org.openscience.cdk.smarts.SmartsPattern` and unique-atom mappings.
- Deprecated shortest-path recomputation was replaced by one reusable `ShortestPaths` object per source atom.
- Aromaticity now uses explicit CDK2 models and cycle finders.
- Fingerprints use typed CDK bit/count fingerprint APIs.
- Readers, atom typing, hydrogen handling, SMIRKS transforms, and 3-D model building use current CDK APIs.

## Execution architecture

The original positive-timeout branch started every descriptor family in a separate thread. The new engine instead runs descriptor families sequentially for each molecule and parallelizes only across molecules. This avoids mutable shared-state hazards and makes output order controllable.

A fixed daemon calculation pool enforces optional deadlines. Molecule worker count, waiting queue size, and output order are explicit. Timeouts are cooperative; no unsafe thread termination is used.

## Determinism corrections

- CDK 2.13's `AtomValenceTool` uses an unsafe lazy static map. The final JAR overlays it with an immutable class-initialized table.
- Information-content category accumulation is sorted before summation, avoiding hash-iteration-order floating-point differences.
- All schema order and duplicate-name resolution use deterministic collections and ordering.
- Directory inputs are processed in case-insensitive filename order.

## Descriptor compatibility

The default 1-D/2-D header remains exactly 1,444 columns in historical order. This is a schema compatibility convenience, not numerical compatibility. Changes in CDK chemistry perception and descriptor implementations are expected and should be treated as a new feature-generation system.

## Deliberately retained legacy component

CDK's optional `IPMolecularLearningDescriptor` is deprecated upstream and has no direct replacement. It remains available only in the all-enabled custom profile and is not active in the default 1,444-column profile. Its use is explicitly isolated and compiler-warning-suppressed.

## ModelBuilder3D concurrency

CDK 2.13 caches `ModelBuilder3D` and `TemplateHandler3D` globally. Their mutable, lazily loaded state is not safe for simultaneous coordinate generation. PaDEL therefore serializes only `-convert3d` coordinate generation. Descriptor evaluation and non-3D molecule preparation remain parallel.

## Negative isotope records in ring templates

The official CDK 2.13 ring-template SDF contains 102 negative `M  ISO` absolute-mass records. CDK rejects and ignores each one while logging an ERROR. The deterministic build removes only those invalid lines before packaging. Generated coordinate and descriptor values were regression-tested before and after sanitization and are unchanged.
