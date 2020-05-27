# krei-central

JShell/krei scripts for the projects available at https://github.com/metio

## Usage

1. Make sure that you have at least Java 14 installed
2. Package krei as a jar file
3. Open JShell with krei and its dependencies

```shell script
$ java -version
$ mvn verify
$ jshell <project>/project.jsh
```

## Available Projects

- `hcf4j`: HTTP client facade for Java
- `ilo`: Reproducible build environments
- `kdk`: Write Java code to generate/validate k8s YAMLs
- `maven-build-process`: Common POMs/BOMs for Maven projects
- `metio-hugo-theme`: Common hugo theme for all projects
- `yosql`: Persistence layer for Java 
