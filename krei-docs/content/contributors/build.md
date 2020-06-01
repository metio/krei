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

The required build environment for `krei` is described in [build-env](../build-env).

In case you have Java and Maven locally installed call:

```shell script
$ mvn verify
```

In case you are using `ilo` call this:

```shell script
$ ilo @build/once
```
