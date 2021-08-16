# Tree transformation

Tree transformations are the core mechanism of processing PSI trees.
It allows to add new information in vertices, hide them or change properties.
Although PSI trees support a big amount of languages, most tree transformations are language-specific.
Therefore, adding a new language requires implementing new behavior to these transformations.
But since not all transformations are useful for a particular language,
e.g. removing Java symbols from Kotlin or Python code,
`PSIMiner` was developed in the way of *lazy* transformations adding.
It means you may to not implement transformation for a new language while you do not need it.

Here is the table of supported transformations for each language.

|                             | Java | Kotlin |
|-----------------------------|------|--------|
| Exclude whitespaces         | ✅    | ✅      |
| Exclude package statement   | ✅    | ⚙️      |
| Exclude import statements   | ✅    | ⚙️      |
| Exclude Java symbols        | ✅    | ❌      |
| Exclude keywords            | ✅    | ⚙️      |
| Exclude empty grammar lists | ✅    | ❓      |
| Compress operators          | ✅    | ❓      |
| Hide literals               | ✅    | ⚙️      |
| Remove comments             | ✅    | ⚙️      |
| Resolve types               | ✅    | ⚙️      |

Legend:\
✅ — supported transformation;\
⚙️ — need to implement transformation;\
❌ — impossible for this language transformation;\
❓ — need to study the correctness of this transformation for this language.

## Transformation description

Here we describe each transformation with its parameters and configuration.

### Exclude whitespaces

Since PSI trees are concrete syntax trees, they contain information about each symbol in the code.
Therefore, for each whitespace, tab and empty line there is a special node in the tree.
This transformation removes them.

```json
{
  "name": "exclude whitespace"
}
```

### Exclude package statement

Remove nodes from PSI tree that correspond to package definition.

```json
{
  "name": "exclude package"
}
```

### Exclude import statements

Remove nodes from PSI tree that correspond to different imports.

```json
{
  "name": "exclude imports"
}
```

### Exclude Java symbols

Since PSI trees are concrete syntax trees, they contain information about each symbol in the code.
For Java code PSI trees contain separate nodes for these symbols:
```
( ) [ ] { } ; , . ... @
```

This transformation removes corresponding nodes from the tree.

```json
{
  "name": "exclude language symbols"
}
```

### Exclude keywords

Remove nodes from trees that correspond to different keywords in code, e.g. `for`, `while`, `if`.

```json
{
  "name": "exclude keywords"
}
```

### Exclude empty grammar lists

Due to peculiarities of PSI trees (at least for Java),
sometimes there are appears nodes for list of nodes, but the list is empty.
For example, if function has no parameters then there will still be a `ParameterList` node in the tree.
The same for list of modifiers, annotations etc.

This transformation removes these empty lists.

```json
{
  "name": "exclude empty grammar lists"
}
```

### Compress operators

In PSI trees (at least for Java) for each operator there are a separate node for operator description.
For example, node with type `BinaryExpression` has three children: first operand, operator symbol, second operand.
This transformation allows compressing operator into the parent node.
So, after applying transformation to example it became `BinaryExpression:OPERATOR` with two operand children.

```json
{
  "name": "compress operators"
}
```

### Hide literals

This transformation allows replacing literals with special mask value.
For example, transformation can replace all strings into `<STR>` value.

```json
{
  "name": "hide literals",
  "hideNumbers": true,
  "numberWhiteList": [0, 1, 32, 64],
  "hideStrings": true
}
```

Parameters:
- `hideNumbers` — if `true` than replace all numbers with `<NUM>` except defined in `numberWhiteList` (default: 
`false`).
- `numberWhiteList` — define list of numbers that should not be masked (default: `[0, 1, 32, 64]`).
- `hideStrings` —if `true` than replace all strings with `<STR>` (default: `false`).

### Remove comments

This transformation allows removing comments from code.

```json
{
  "name": "remove comments",
  "removeDoc": true
}
```

Parameters:
- `removeDoc` — special parameter to define removing language-specific documentation.
For example, JavaDoc for Java, KDoc for Kotlin, DocString for Python.
Default value is `true`.

### Resolve types

Transformation for resolving types for different nodes in PSI trees.
For example, resolve type for each variable or function call.
For more details, see our [paper](https://ieeexplore.ieee.org/document/9463105).

This transformation changes storage behavior a little:
- JSON storage for trees would contain `tokenType` field for each node.
- code2seq storage would contain path contexts with five elements instead of three:
`start token type`, `start token`, `path node types`, `end token`, `end token type`.

```json
{
  "name": "resolve type"
}
```
