Notes:

1. In `build.gradle` make `localPath` point to your local Idea installation. Execute `buildPlugin` gradle task. 
Then open your Idea and "install the plugin from disk" at `build/distributions/test-1.0-SNAPSHOT.zip`. 

1. Download a raw dataset of java projects. For example, `java-med`. It should contain 3 folders: `train`, `test` and `val`.

1. Run `idea code2vec /home/java-med /home/java-med-mined` with appropriate paths to `java-med` and so on. 
   Please, adopt the paths processing things in `Runner.kt` as they are a bit hard-coded at the moment.
   Lines 75-95. You may also want to change the number of expected entries for ChronicleMaps specified in Storage.kt.

> On Mac OS, `idea` is somewhere similar to `~/Library/Application\ Support/JetBrains/Toolbox/apps/IDEA-C/ch-0/201.7846.29/IntelliJ\ IDEA\ CE.app/Contents/MacOS/idea` 

Let it run until it's finished (`java-med` took ~23 hours to compute on my laptop).
You'll get `gold` and `withTypes` folders under `/home/java-med-mined`. 
The first one is the astminer result with `undefined` types. 
The `withTypes` folder contains the results of using astminer together with IntelliJ PSI information.

Refs for `java-med`:

```bash
# with types
> wc -l *
  352.675   tokens_types.csv
  4.717.711 tokens.csv
  744.259   paths.csv
  574       node_types.csv

  445.034   test_path_contexts.csv
  3.275.140 train_path_contexts.csv
  454.352   val_path_contexts.csv
```

```bash
# gold
> wc -l *
  2         tokens_types.csv 
  2.848.739 tokens.csv
  779.687   paths.csv 
  742       node_types.csv

  423.565   test_path_contexts.csv
  3.158.038 train_path_contexts.csv
  436.146   val_path_contexts.csv  
```