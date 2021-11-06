# Filters

Each filter is dedicated to removing *bad* trees from the data, e.g. trees that are too big.
Moreover, each filter works only for certain levels of granulaity.
Here we describe all filters provided by `PSIMiner`.
Each description contains the corresponding JSON config.

### Filter by tree size
**granularity**: files, functions

Exclude trees that are too small or too big by counting the amount nodes in it.

```json
{
  "name": "tree size",
  "minSize": 0,
  "maxSize": null
}
```

`minSize` and `maxSize` are optional parameters.

### Filter by amount lines of code
**granularity**: files, functions

Exclude trees that correspond to code with to small or to many lines.
Each code snippet normalized before counting lines, e.g. remove lines with only parenthesis.

```json
{
  "name": "code lines",
  "minCodeLines": 0,
  "maxCodeLines": null
}
```

`minCodeLines` and `maxCodeLines` are optional parameters.

### Exclude constructors
**granularity**: functions

Exclude constructors.

```json
{
  "name": "constructor"
}
```

### Filter by annotation
**granularity**: functions

Exclude functions that have certain annotations (e.g. `@Override`)

```json
{
  "name": "by annotations",
  "excludeAnnotations": ["Override"]
}
```

### Filter by modifiers
**granularity**: functions

Exclude functions with certain modifiers (e.g. `abstract` functions)

```json
{
  "name": "by modifiers",
  "excludeModifiers": ["abstract"]
}
```

### Exclude empty functions
**granularity**: functions

Exclude functions with empty body and functions without body

```json
{
  "name": "empty method"
}
```

## New filters

To add new filter following next steps:
1. Implement [filter](../psiminer-core/src/main/kotlin/filter/Filter.kt) interface.
2. Add filter [config](../psiminer-cli/src/main/kotlin/config/FilterConfigs.kt) for it.
3. Register filter config in tool [runner](../psiminer-cli/src/main/kotlin/PluginRunner.kt).
4. [Optional] Add tests for this filter.
