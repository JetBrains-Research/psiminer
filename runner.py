from argparse import ArgumentParser
from pathlib import Path
import os


def process_project(output_dir: Path, input_dir: Path, config_path: Path):
    command = f"bash psiminer.sh {input_dir} {output_dir} {config_path}"
    print(f"Running {command}")
    os.system(command)


def process(o: Path, p: Path, c: Path):
    projects = [folder for folder in os.listdir(p) if os.path.isdir(p / folder)]
    for project in projects:
        output_dir = o / project
        input_dir = p / project
        process_project(output_dir, input_dir, c)


if __name__ == '__main__':
    parser = ArgumentParser()
    parser.add_argument("-o", required=True)
    parser.add_argument("-p", nargs="*", required=True)
    parser.add_argument("-c", required=True)
    args = parser.parse_args()

    paths = [Path(p) for p in args.p]
    output = Path(args.o)
    config = Path(args.c)
    for p in paths:
        process(output, p, config)
