---
title: 'Core: Usage'
date: 2020-04-13
menu:
  main:
    parent: 'krei-core'
categories:
- 'krei-core'
tags:
- usage
---

In order to create a new project with `krei` you will have to follow these steps:

1. Define a project: Create a `Project` or `ProjectConfiguration`
2. Generate a project: Materialize the above definition to the file system
3. Validate a project: (Optional) validate definition against best-practices and check generated project against definition.
4. **Celebrate \o/**

## Define Projects

`krei-core` offers a high-level declarative API and a low-level imperative API.

### Declarative API

The main interface of the declarative API is `ProjectConfiguration`. Create a new instance like this:

```java
import wtf.metio.krei.model.*;
import wtf.metio.krei.model.build.maven.*;
import wtf.metio.krei.model.ide.idea.*;
import wtf.metio.krei.model.license.*;
import wtf.metio.krei.model.vcs.git.*;
import wtf.metio.krei.template.*;

var projectName = "your-new-project";
var license = License.of(LicenseTemplates.CC0, projectName);
var git = Git.builder()
        .ignore(GitIgnore.of(GitIgnoreTemplates.MAVEN).and(GitIgnoreTemplates.BAZEL))
        .config(GitConfig.of("user.email", "someone@example.com"))
        .addRemotes(GitRemote.github("org/repo"))
        .build();
var idea = Idea.builder()
        .copyright(IdeaCopyright.builder().build())
        .codeStyle(IdeaCodeStyle.builder().build())
        .build();
var maven = Maven.builder()
        .build();
var project = ProjectConfig.builder(projectName)
        .license(license)
        .vcs(git)
        .addIde(idea)
        .build(maven)
        .build();
```

### Imperative API

## Materialize Projects

## Validate Project

## Celebrate

Thanks for trying out `krei`! In case you are missing something, please [let us know](../../community/help). In case you want to show that you used `krei` for one of your projects, consider using these badges:

![krei generated this project](https://img.shields.io/badge/krei-created-success) for projects that were once created with `krei` use `https://img.shields.io/badge/krei-created-success`.

![krei manages this project](https://img.shields.io/badge/krei-managed-success) for projects that are continuously monitored by `krei` use `https://img.shields.io/badge/krei-managed-success`.
