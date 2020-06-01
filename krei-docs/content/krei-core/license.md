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
import wtf.metio.krei.template.LicenseTemplates;

var projectName = "your-project";
var license = License.of(LicenseTemplates.CC0, projectName);
```

- [x] configure `LICENSE` file
- [x] configure `AUTHORS/WAIVER` file
- [x] configure source file header
- [x] configure `README` license section
- [x] use provided project name for templating

### Custom Configuration

```java
import wtf.metio.krei.model.license.License;
import wtf.metio.krei.model.license.Waiver;
import java.nio.file.Paths;

var projectName = "your-project";
var license = License.builder()
    .license("your license text")
    .licenseFile(Paths.get("/path/to/the/LICENSE/file"))
    .sourceHeader("your source file header")
    .sourceHeaderMatcher("regex to match source header")
    .readme("README text for your license")
    .waiver(Waiver.builder()
        .text("your waiver text")
        .waiverFile(Paths.get("/path/to/the/WAIVER/file"))
        .build())
    .build();
```

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

```java
import wtf.metio.krei.unit.file.LicenseUnits;

var projectDirectory = Paths.get("/some/project/folder");
var waiverFile = projectDirectory.resolve("AUTHORS/WAIVER");
var waiverText = "your waiver text";
var waiver = LicenseUnits.createWaiver(waiverFile, waiverText);
```

## License Templates

`krei` includes a collection of pre-defined license templates which all be accessed through the [`LicenseTemplates`](https://github.com/metio/krei/blob/master/krei-core/src/main/java/wtf/metio/krei/template/LicenseTemplates.java) enum.

Available license templates include:

- Creative Commons Zero 1.0
