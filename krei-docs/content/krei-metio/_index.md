---
title: 'krei-metio'
date: 2020-04-13
---

`krei-metio` contains definitions for projects at https://github.com/metio. You can use it as a reference on how to create your own set of rules for projects.

It uses the [jshell-maven-plugin](https://github.com/johnpoth/jshell-maven-plugin) to open an interactive development environment in order to perform dry-runs, materialize new projects, and validate existing ones. 

Use one of the following commands to open a new JShell session:

```shell script
# using maven
$ mvn --projects krei-metio --also-make --activate-profiles jshell test

# using make
$ make jshell

```

See [Makefile](../../contributors/makefile) for details about `make` usage.
