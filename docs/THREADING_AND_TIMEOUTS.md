# Threading and timeout behavior

`-threads N` selects the maximum number of molecules evaluated concurrently. Omitting it or using `-threads -1` uses all processors visible to Java. OPERA distributions should normally pass a resolved positive number.

`-waitingjobs N` controls queued, parsed molecules. It does not add active descriptor workers. Four queued molecules per worker is a balanced default.

`-maxruntime MS` is a deadline for the complete preparation and descriptor calculation of one molecule. Descriptor families remain sequential inside that deadline. When it expires, the result row is returned with unfinished values marked missing and the molecule recorded as timed out.

Cancellation uses interruption. Java cannot safely force-stop arbitrary code in a thread. The calculation pool is bounded and daemonized, so ignored interruption cannot create unbounded non-daemon threads or prevent process exit. For an absolute kill guarantee, OPERA must isolate work in a child JVM and terminate that process.

## 3-D coordinate generation

CDK 2.13's cached `ModelBuilder3D` and ring-template handler are process-wide mutable objects. `-convert3d` coordinate generation is therefore protected by a global lock. This avoids corrupted template loading and mapping. Molecules continue through descriptor calculation in parallel after coordinates are generated. `-retain3d` does not require this lock.
