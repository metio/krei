---
title: Build
date: 2020-04-13
menu:
  main:
    parent: 'Contributors'
categories:
- Contributors
tags:
- build
---

The required build environment for `krei` is described in [dev-env](../dev-env).

In case you have Java and Maven locally installed call:

```shell script
# run all tests
$ mvn verify

# install locally
$ mvn install

# open jshell
$ mvn --projects krei-metio --also-make --activate-profiles jshell test
```

In case you are using `ilo` call this:

```shell script
# build the project
$ ilo @build/once

# open a shell with a pre-defined build environment
$ ilo @build/shell
```

Take a look at the [Makefile](../makefile) as an easy way to call all these commands.
