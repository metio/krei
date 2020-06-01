---
title: 'Metio: Maven'
date: 2020-04-13
menu:
  main:
    parent: 'krei-metio'
categories:
- 'krei-metio'
tags:
- usage
- maven
---

All [Maven](https://maven.apache.org/) projects at https://github.com/metio adhere to a certain project setup/layout which includes:

- [x] Use Creative Commons Zero [license](../../krei-core/license/#license-templates)
- [x] Create `LICENSE`
- [x] Create `AUTHORS/WAIVER`
- [x] Create `README.md`
- [x] Create `CODE_OF_CONDUCT.md`
- [x] Create `CONTRIBUTING.md`
- [x] Create `CHANGELOG.md`
- [x] Create `.github/SECURITY.md`
- [x] Create `.github/SUPPORT.md`
- [x] Create templates in `.github/ISSUE_TEMPLATE/`
- [x] Create `.github/ISSUE_TEMPLATE/config.yml`
- [x] Create templates in `.github/PULL_REQUEST_TEMPLATE/`
- [x] Create `.github/workflows/release.yml`
- [x] Create `Makefile`
- [x] Create `pom.xml`
- [x] Use [maven-build-process](https://github.com/metio/maven-build-process) as Maven parent
- [x] Configure IntelliJ Idea code style settings
- [x] Configure IntelliJ Idea copyright settings
- [x] Create `docs` folder with [metio-hugo-theme](https://github.com/metio/metio-hugo-theme) as theme
- [x] Initialize Git repository
- [x] Setup local Git configuration
- [x] Create `.gitignore`
- [x] Perform initial commit
- [x] Create Git remotes at GitHub/GitLab/Codeberg/BitBucket
- [x] Push to all remotes

You can do all that manually, or call the following code:

```java
import wtf.metio.krei.metio.shared.*;

var project = MetioMavenProject.configure("projectName");
```

The JShell plugin ([see here](../)) already imports all packages, thus you can just write:

```java
var project = MetioMavenProject.configure("projectName");
```

You can see its current implementation of `MetioMavenProject` [here](https://github.com/metio/krei/blob/master/krei-metio/src/main/java/wtf/metio/krei/metio/shared/MetioMavenProject.java).
