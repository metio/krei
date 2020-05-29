---
title: Git Mirrors
date: 2020-04-13
menu:
  main:
    parent: 'Contributors'
---

`krei` is using [GitHub](https://github.com/metio/krei) as its primary source/issue/build environment, however its code is available in other git repositories as well in order to deal with [GitHub outages](https://www.githubstatus.com/).

## SSH Setup (**Optional**)

Use the following example config as reference and adapt according to your needs. In case you are using the same SSH key for every remote, adapt the `IdentityFile` part in the example. In case you only want to mirror to a subset of all mirrors, remove all those that you do not want to support.

```
Host github
    HostName github.com
    User git
    IdentityFile ~/.ssh/github

Host gitlab
    HostName gitlab.com
    User git
    IdentityFile ~/.ssh/gitlab

Host bitbucket
    HostName bitbucket.org
    User git
    IdentityFile ~/.ssh/bitbucket

Host codeberg
    HostName codeberg.org
    User git
    IdentityFile ~/.ssh/codeberg
```

## Git Remote Mirrors

Use the following commands to create a new git remote which only allows pushing code. Only specify those mirrors that you want to support.

```shell script
$ git remote add mirrors DISABLED
$ git remote set-url --add --push mirrors codeberg:metio.wtf/krei.git
$ git remote set-url --add --push mirrors gitlab:metio.wtf/krei.git
$ git remote set-url --add --push mirrors bitbucket:metio-wtf/krei.git
```

In case you have not done the SSH setup above, use this:

```shell script
$ git remote add mirrors DISABLED
$ git remote set-url --add --push mirrors git@codeberg.org:metio.wtf/krei.git
$ git remote set-url --add --push mirrors git@gitlab.com:metio.wtf/krei.git
$ git remote set-url --add --push mirrors git@bitbucket.org:metio-wtf/krei.git
```

Finally, push the current branch to all mirrors, e.g. `master`:

```shell script
$ git push mirrors master
```
