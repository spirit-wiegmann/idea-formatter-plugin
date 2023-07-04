# Idea Formatter Plugin

[![Build](https://github.com/funbiscuit/idea-formatter-plugin/actions/workflows/ci.yaml/badge.svg?branch=master)](https://github.com/funbiscuit/idea-formatter-plugin/actions/workflows/ci.yaml)

Tool to format and rearrange code using Intellij IDEA that works in CI.

It is very similar to embedded script in IDEA `format.sh`, but it also rearranges code
while `format.sh` and many other existing tools only formats code.

## How to use

This repository contains an Intellij IDEA plugin that adds new command to CLI - formatter:

```shell
idea.sh formatter -h
```

To use it more conveniently (without downloading proper version of IDEA), plugin and IDE are packaged into
executable docker image: [funbiscuit/idea-formatter](https://hub.docker.com/r/funbiscuit/idea-formatter).

To use it you can either execute docker container or create container beforehand and execute `formatter` inside it.

### Docker run

Just pass project files via volume and run image:

```shell
docker run --rm -v $PROJECT_PATH:/data funbiscuit/idea-formatter --style CodeStyle.xml --dry --recursive src
```

`$PROJECT_PATH` - is a path to your project on host machine. `CodeStyle.xml` - code style settings (must be
inside `$PROJECT_DIR`). `src` - list of directories to format/check (must be inside `$PROJECT_DIR`).
Working directory in image is `/data` by default.

### Docker exec

```shell
# Launch container and leave it in background
docker run -it --entrypoint cat --rm -v $PROJECT_PATH:/data funbiscuit/idea-formatter
# Connect to container
docker exec -it $CONTAINER_ID /bin/bash
# Inside container run formatter
formatter --style CodeStyle.xml --dry --recursive src
```

This approach might be more convenient if you automatically spin up container in CI
and then can execute commands inside it.

## Arguments

To get list of available options you can execute:

```shell
docker run --rm -it funbiscuit/idea-formatter -h
```

|     Option      | Example           | Description                                                                    |
|:---------------:|-------------------|--------------------------------------------------------------------------------|
|    -d, --dry    |                   | Perform a dry run: no file modifications, only exit status                     |
|       -h        |                   | Show this help message and exit.                                               |
|   -m, --mask    | \*.java,\*.groovy | A comma-separated list of file masks. Use quotes to prevent wildcard expansion |
| -r, --recursive |                   | Scan directories recursively                                                   |
|   -s, --style   | CodeStyle.xml     | A path to Intellij IDEA code style settings .xml file                          |

## License

Licensed under either of

* Apache License, Version 2.0
  ([LICENSE-APACHE](LICENSE-APACHE) or http://www.apache.org/licenses/LICENSE-2.0)
* MIT license
  ([LICENSE-MIT](LICENSE-MIT) or http://opensource.org/licenses/MIT)

at your option.

## Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be
dual licensed as above, without any additional terms or conditions.
