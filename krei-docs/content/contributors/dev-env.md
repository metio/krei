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

`krei` requires a certain set of software installed on your system in order to be built.

## Prerequisites

- [git](https://git-scm.com/) for version control
- [minisign](https://jedisct1.github.io/minisign/) to sign the waiver as a contributor
- [gpg](https://gnupg.org/) to sign your the commit which adds the waiver

## Manual Setup

- [Java JDK](https://jdk.java.net/) to compile the code
- [Maven](https://maven.apache.org/) to build the project
- [hugo](https://gohugo.io/) in order to create the website

## ilo Setup

- [ilo](https://github.com/metio/ilo) to open the predefined build environment
- [podman](https://podman.io/) or [docker](https://www.docker.com/) as an `ilo` runtime

## GitHub Packages

Some of `krei`'s dependencies are only available in GitHub packages which requires authentication. Since `krei` is using Maven to build itself, you will have to create/change your local `~/.m2/settings.xml` file. Use the following template as reference and take a look at the [settings used by krei](https://github.com/metio/krei/blob/master/build/settings.xml) itself:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>YOUR_NAME</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>YOUR_NAME</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          <releases>
	          <enabled>true</enabled>
	        </releases>
          <snapshots>
	          <enabled>false</enabled>
	        </snapshots>
        </repository>
        <repository>
          <id>maven-build-process</id>
          <name>GitHub maven-build-process Apache Maven Packages</name>
          <url>https://maven.pkg.github.com/metio/maven-build-process</url>
          <releases>
	          <enabled>true</enabled>
	        </releases>
          <snapshots>
	          <enabled>true</enabled>
	        </snapshots>
        </repository>
        <repository>
          <id>hcf4j</id>
          <name>GitHub hcf4j Apache Maven Packages</name>
          <url>https://maven.pkg.github.com/metio/hcf4j</url>
          <releases>
	          <enabled>true</enabled>
	        </releases>
          <snapshots>
	          <enabled>true</enabled>
	        </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>maven-build-process</id>
      <username>YOUR_NAME</username>
      <password>YOUR_TOKEN</password>
    </server>
    <server>
      <id>hcf4j</id>
      <username>YOUR_NAME</username>
      <password>YOUR_TOKEN</password>
    </server>
  </servers>
</settings>
```

Replace `YOUR_NAME` and `YOUR_TOKEN` with your GitHub username and [personal access token](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line).
