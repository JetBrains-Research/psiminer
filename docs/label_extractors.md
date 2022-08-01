# Label extractors

Label extractors are required for correct extraction of labels from raw trees.
Internally, they extract labels from the tree and process the tree to avoid data leaks.
Also, label extractors define the granularity level for the pipeline.

### function name

**granularity**: functions

Use name of each function as a label.
This label extractor will also hide the function name in the AST and all recursive calls to prevent data leaks.

```json
{
  "name": "method name"
}
```

### function comments

**granularity**: functions

Use comments of each function as a label, skipping functions without comments.
By default, this label extractor finds doc comments, related to function, leaving only description part (skipping
params, return and so on). You can also specify using all comments, including inline and block.

```json
{
  "name": "method comment" // only doc by default
}
```

Or:

```json
{
  "name": "method comment",
  "onlyDoc": false // Use all comments
}
```

## New label extractors

To add new label extractor following next steps:

1. Implement [LabelExtractor](../psiminer-core/src/main/kotlin/labelextractor/LabelExtractor.kt) interface.
2. Add label extractor [config](../psiminer-cli/src/main/kotlin/config/LabelExtractorConfigs.kt) for it.
3. Register config in tool [runner](../psiminer-cli/src/main/kotlin/PluginRunner.kt).
4. [Optional] Add tests for this label extractor.
