# Additional preprocessing

`PSIMiner` can perform additional preprocessing increasing the chances that projects in the dataset are opened correctly by IDEA.
However, additional preprocessing **mutates** the original dataset: **adds**, **removes** or **updates** files.

To enable additional preprocessing add a `preprocessing` field in the config and setup name.
For now, `PSIMiner` supports only additional preprocessing for Java or Kotlin repositories.
For example:

```json
{
  "preprocessing": {
    "name": "jvm"
  }
}
```

## Types of preprocessing

### JVM preprocessing
1. Deleting `.idea` folders.
2. Adding `local.properties` files with path to Android SDK.
Without it Android projects are opened incorrectly.

To make sure that more Android projects are opened correctly you should set the `androidSdk`
field in the config to the Android SDK path (`$ANDROID_HOME`):

```json
{
  "preprocessing": {
    "name": "jvm",
    "androidSdkHome": "/absolute/path/to/android/home"
  }
}
```
