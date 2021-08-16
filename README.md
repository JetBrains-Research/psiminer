# `PSIMiner`

[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

`PSIMiner` — a tool for processing PSI trees from the IntelliJ Platform.
PSI trees contain code syntax trees as well as functions to work with them,
and therefore can be used to enrich code representation using static analysis algorithms of modern IDEs.

`PSIMiner` is a plugin for IntelliJ IDEA that runs it in a headless mode and creates datasets for ML pipelines.

The complete documentation of different parts is stored in [docs](./docs) folder.

## Installation

`PSIMiner` requires Java 11 for correct work.
Check gradle will use the correct version.
All other dependencies will be installed automatically.

Use `./gradlew build` (or `gradlew.bat build` on Windows) to build the tool.

## Usage

There are already predefined configurations compatible with the IntelliJ IDEA.
Open or import project in it and run tool on test data or start tests.
You can modify these configurations to suit your needs.

However, it is possible to run the tool through CLI.
It is better to use predefined shell script (only for Unix system)
```shell
./psiminer.sh $dataset_path $output_folder $JSON_config
```

### Logs

`PSIMiner` automatically store logs in home directory of user on each run.
Check `~/psiminer.log` (or something like `C:\Users\yourusername\psiminer.log` for Windows) and share it to describe 
your 
problem.

## Configuration

`PSIMiner` completely configured by JSON.
Check examples in the [configs](configs) folder.

Logically `PSIMiner` consist of the following parts.
There are a full documentation for them in [docs](./docs) folder:
- [Tree transformations](./docs/tree_transormations.md) —
this is an interface for enriching trees with new information and other useful manipulations,
e.g. resolve types or exclude whitespaces.
- [Filters](./docs/filters.md) —
this is an interface for removing *bad* trees from the data, e.g. trees that are too big.
- [Label extractor](./docs/label_extractors.md) —
this is an interface to define the correct extraction of labels from raw trees,
e.g. extract method name for each method.
- [Storage](./docs/storages.md) —
this is an interface to define how tree should be saved on the disk,
e.g. code2seq format or JSONL format.

There are also a few fields to define a parser and pipeline options.
For example, setting up `Language`.

## Language support

Currently, `PSIMiner` supports `Java` and `Kotlin` datasets.
But we developed the tool with the possibility to extend it to new languages.
And since `PSI` trees supports big amount of languages,
adding new language into the tool requires only implementing few interfaces.

Be aware that multiple tree transformations can't be adopted to new languages automatically.
And therefore, require manual work to add support for the new language.

If you would like to see new languages, don't hesitate to create issues with their request.
Or even implement them yourself and create a pull request.

## Use as dependency

You can reuse different parts of the `PSIMiner` inside your one tool, e.g. plugin for model inference.
To add core part of the tool (without dependency to CLI) add following code into your gradle.kts file:
```
dependencies {
    implementation("org.jetbrains.research.psiminer:psiminer-core") {
        version {
            branch = "main"
        }
    }
}
```

Remember that `PSIMiner` is *plugin* for IntelliJ IDEA and, therefore, can be integrated only in another plugin.

## Citation

The [paper](https://ieeexplore.ieee.org/document/9463105)
dedicated to the `PSIMiner` was published in MSR'21.
If you use `PSIMiner` in your academic work, please, cite it.
```
@inproceedings{spirin_psiminer,
  author={Spirin, Egor and Bogomolov, Egor and Kovalenko, Vladimir and Bryksin, Timofey},
  booktitle={2021 IEEE/ACM 18th International Conference on Mining Software Repositories (MSR)}, 
  title={PSIMiner: A Tool for Mining Rich Abstract Syntax Trees from Code}, 
  year={2021},
  pages={13-17},
  doi={10.1109/MSR52588.2021.00014}
}
```

## Additional preprocessing

PSIMiner can perform additional preprocessing increasing the changes that projects in the dataset are opened correctly by IDEA.
However, additional preprocessing **mutates** the original dataset: **adds**, **removes** or **updates** files.

To enable additional preprocessing add a `additional preprocessing` field in the config and set `enable: true`:

```json
{
  "additional preprocessing": {
    "enable": true
  }
}
```

### Types of preprocessing

#### Deleting `.idea` folders.

It is always turned on when `additional preprocessing` is enabled.

#### Adding `local.properties` files with path to Android SDK. Without it Android projects are opened incorrectly.

To make sure that more Android projects are opened correctly you should set the `androidSdk`
field in the config to the Android SDK path (`$ANDROID_HOME`):

```json
{
  "additional preprocessing": {
    "enable": true,
    "androidSdk": "/absolute/path/to/android/home"
  }
}
```
