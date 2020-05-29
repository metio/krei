---
title: Dev Env
date: 2020-04-13
menu:
  main:
    parent: 'Contributors'
categories:
- Contributors
tags:
- environment
---

## minisign

Every contributor has to sign the [WAIVER](https://github.com/metio/krei/blob/master/AUTHORS/WAIVER) in order to signal that all commits can be published into the public domain. In order to sign the waiver, [minisign](https://jedisct1.github.io/minisign/) has to be installed on your system. Once installed, make sure at least version `0.8` is installed:

```shell script
$ minisign -v
minisign 0.8
```

In order to sign the waiver, switch into the `AUTHORS` directory and call the following commands:

```shell script
$ minisign -Sm WAIVER
$ mv WAIVER.minisign WAIVER.`id --name --user`.minisign
```

In order to verify existing signatures, do any of the following:

{{< minisignatures >}}
