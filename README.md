# PSIMiner

The tool for processing PSI trees from IntelliJ IDEA and creating labeled dataset from them.

## Usage

To run extracting data from a dataset with source code use this command
```shell
bash psiminer.sh dataset_path ouput_folder config.json 2> error_log.txt
```

Folder with [mock data](psiminer-core/src/test/resources/mock_data) contains example of dataset.
[Config](config.json) shows the example of configuration.
To run this example use following command:
```shell
bash psiminer.sh src/test/resources/mock_data output config.json 2> error_log.txt
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
