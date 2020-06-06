# krei build tooling

This directory contains [ilo](https://ilo.projects.metio.wtf) argument files, a [Maven](maven.apache.org/) settings file, and a [Docker](docker.com/) image which contains all build dependencies.

## ilo

- `once`: `ilo` arguments to build the entire project once
- `shell`: `ilo` arguments to open a new shell with all build dependencies

## Maven

- `settings.xml`: Maven settings that connects to GitHub Packages

## Docker

- `/env/`: Docker image with all build dependencies.
