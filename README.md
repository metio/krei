# krei

Project-as-code approach for project creation & setup.

## Usage

```java
var projectDirectory = Paths.get("/some/folder");
var license = projectDirectory.resolve("LICENSE");

var project = Project.builder(projectDirectory)
    .addUnits(GitUnits.initializeRepository(projectDirectory))
    .addUnits(LicenseUnits.createLicense(license, License.CC0))
    .build();
```

## Alternatives

- https://github.com/kowainik/summoner (Haskell)
- https://github.com/martinklepsch/tenzing (ClojureScript)
- https://github.com/bit101/ProjectMaker (Sublime)
- https://github.com/generate/generate (NPM)
- https://yeoman.io/ (NPM)
- https://jeka.dev/ (Java)

## Links

- [OpenHub](https://www.openhub.net/p/krei)

## SonarQube

||
|------:|
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=alert_status)](https://sonarcloud.io/dashboard?id=metio_krei) |
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=metio_krei) |
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=security_rating)](https://sonarcloud.io/dashboard?id=metio_krei) |
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=metio_krei) |
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=ncloc)](https://sonarcloud.io/dashboard?id=metio_krei) |
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=bugs)](https://sonarcloud.io/dashboard?id=metio_krei) |
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=metio_krei) |
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=code_smells)](https://sonarcloud.io/dashboard?id=metio_krei) |
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=sqale_index)](https://sonarcloud.io/dashboard?id=metio_krei) |
| [![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=metio_krei&metric=coverage)](https://sonarcloud.io/dashboard?id=metio_krei) |

## License

```
To the extent possible under law, the author(s) have dedicated all copyright
and related and neighboring rights to this software to the public domain
worldwide. This software is distributed without any warranty.

You should have received a copy of the CC0 Public Domain Dedication along with
this software. If not, see http://creativecommons.org/publicdomain/zero/1.0/.
```

## Mirrors

- https://github.com/metio/krei
