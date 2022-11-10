# Storages

The storage defines how the trees should be saved on disk.
For now, `PSIMiner` support tree-based and path-based storage formats.

`PSIMiner` also knows how to find the structure of the dataset and can
save input data in the appropriate holdout folders (`train`, `val` and `test`).
If the data is not structured, all trees will be saved in the `output` folder in one file.

## Plain text format

Save just method code with applied tree transformations to `.jsonl` file
```json
{
  "name": "raw code"
}
```
## Tree formats

### JSON Lines

Saves each tree with its label in the Json Lines format.
Json format of AST inspired by the [150k Python](https://www.sri.inf.ethz.ch/py150) dataset.

```json
{
  "name": "json tree"
}
```

## Path-based representations

Path-based representation was introduced by [Alon et al.](https://arxiv.org/abs/1803.09544).
It is used in popular code representation models such as `code2vec` and `code2seq`.

### Code2seq

Extract paths from each AST and save in the code2seq format.
The output is `path_context.c2s` file, which will be generated for every holdout.
Each line starts with a label, followed by a sequence of space-separated triples.
Each triple contains the start token, path node types, and end token, separated with commas.

To reduce memory usage, you can enable `nodesToNumbers` option.
If `nodesToNumbers` is set to `true`,
all types are converted into numbers and `node_types.csv` is added to output files.

```json
{
  "name": "code2seq",
  "pathWidth": 2,
  "pathLength": 9,
  "maxPathsInTrain": 1000,
  "maxPathsInTest": 200,
  "nodesToNumbers": true
}
```

`maxPathsInTrain`, `maxPathsInTest`, and `nodesToNumbers` are optional parameters.

## New storages

To add new storage following next steps:
1. Implement [storage](../psiminer-core/src/main/kotlin/storage/Storage.kt) interface.
2. Add storage [config](../psiminer-cli/src/main/kotlin/config/StorageConfigs.kt) for it.
3. Register storage config in tool [runner](../psiminer-cli/src/main/kotlin/PluginRunner.kt).
4. [Optional] Add tests for this storage.

## Metadata collection

To enable metadata collection add additional parameter:

```json
{
  "collectMetadata": true
}
```

This will collect additional data (filepath and range of element presented by tree) in 
the `metadata` folder in form of json lines.