---
title: 'Core: License'
date: 2020-04-13
menu:
  main:
    parent: 'krei-core'
categories:
- 'krei-core'
tags:
- usage
- license
---

`krei` can manage the license for your projects.

## Declarative API

### Standard Configuration

```java
import wtf.metio.krei.model.license.License;
import static wtf.metio.krei.template.LicenseTemplates.CC0;

var projectName = "your-project";
var license = License.of(CC0, projectName);
```

- [x] configure `LICENSE` file
- [x] configure `AUTHORS/WAIVER` file
- [x] configure source file header
- [x] configure `README` license section
- [x] use provided project name for templating

## Imperative API

### Create `LICENSE`

```java
import wtf.metio.krei.unit.file.LicenseUnits;

var projectDirectory = Paths.get("/some/project/folder");
var licenseFile = projectDirectory.resolve("LICENSE");
var licenseText = "your license text";
var license = LicenseUnits.createLicense(licenseFile, licenseText);
```

### Create `WAIVER`

```jshelllanguage
import wtf.metio.krei.unit.file.LicenseUnits;

var projectDirectory = Paths.get("/some/project/folder");
var waiverFile = projectDirectory.resolve("AUTHORS/WAIVER");
var waiverText = "your waiver text";
var waiver = LicenseUnits.createWaiver(waiverFile, waiverText);
```
