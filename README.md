<!--
SPDX-FileCopyrightText: The krei Authors
SPDX-License-Identifier: CC0-1.0
-->

# krei

krei keeps files that many repositories share, such as a `LICENSE` or the absence of a retired config file, in line with rules written once. It reads the repositories from the inventory [aro](https://github.com/metio/aro) writes, compares each default branch with the rules, and proposes the difference as a pull request. It runs on [babashka](https://babashka.org) and calls `git` and, for pull requests on GitHub, `gh`.

## Commands

```shell
krei rules              # list the configured rules
krei list [REPO...]     # list the managed repositories
krei plan [REPO...]     # show what each repository needs to follow its rules
krei check [REPO...]    # like plan, exiting non-zero when anything differs
krei sync [REPO...]     # commit the changes onto a branch, push it and open a pull request
```

`REPO` is a glob over repository names or `host/path` ids, such as `aro` or `github.com/metio/*`. Without one, every managed repository is used, and a `REPO` that matches none is an error. The config is read from `$XDG_CONFIG_HOME/krei/config.edn` unless `--config` names another file.

Each command works on a clone below `$XDG_CACHE_HOME/krei/<host>/<path>`, fetched and reset to the default branch on every run, so local clones elsewhere are never touched. `krei sync` commits onto the branch `krei/sync`, started from the default branch, and force-pushes it. When the branch on the remote already holds the same files, nothing is pushed. On `github.com` it then opens a pull request from that branch unless one is open already; on other hosts it pushes the branch and leaves the pull request to you. Commits are signed off and follow your git config, including commit signing.

## Configuration

```clojure
{:inventory "~/.local/state/aro/inventory.edn"
 :include ["github.com/metio/*"]
 :exclude ["github.com/metio/scratchpad"]
 :archived false
 :forks false
 :rules [:license :citation :authors :mailmap :security :code-of-conduct :contributing
         :no-health-files :editorconfig]
 :rule-dirs ["~/.config/krei/rules"]
 :facts {"*" {:authors [{:given-names "Ada" :family-names "Lovelace" :email "ada@example.org"
                         :old-emails ["ada@old.example.org"]}]
              :security-email "security@example.org"
              :conduct-email "conduct@example.org"}
         "github.com/metio/*" {:health-files-from "github.com/metio/.github"}
         "github.com/metio/.github" {:health-files-from nil :copyright "The metio Authors"}
         "github.com/metio/legacy" {:skip [:editorconfig]}
         "github.com/metio/aro" {:license "0BSD" :keywords ["git" "tmux"]}}
 :branch "krei/sync"
 :message "chore: align repository files with krei rules"
 :signoff true}
```

Every key is optional. Without them krei manages every repository of the inventory at `$XDG_STATE_HOME/aro/inventory.edn` with the `license` rule, reads rules from `$XDG_CONFIG_HOME/krei/rules`, and uses the branch, message and sign-off shown above. `:include` and `:exclude` take the same globs as `REPO`. Archived repositories and forks are left alone unless `:archived` or `:forks` is true.

## Facts

A rule reads what krei knows about a repository from its facts. Later sources override earlier ones:

1. the inventory record: `:host`, `:path`, `:name`, `:namespace`, `:default-branch`, `:archived`, `:fork`, `:topics`;
2. facts read from the checkout: `:license` is the identifier of the only file in `LICENSES/`, and unset when there are several or none; `:go`, `:maven`, `:cargo`, `:npm`, `:babashka` and `:nix` are true when `go.mod`, `pom.xml`, `Cargo.toml`, `package.json`, `bb.edn` or `flake.nix` is at the top of the checkout;
3. `:facts` under `"*"`;
4. `:facts` under glob patterns, from the least to the most specific;
5. `:facts` under the exact repository id.

Derived facts fill in whatever none of these set: `:title` is the `:name`, `:copyright` is `The <title> Authors`, `:homepage` is `https://<host>/<path>/`, and `:keywords` are the `:topics`. Each of `:authors` is a map of `:given-names`, `:family-names`, an optional `:email`, `:old-emails` and a `:role` of `:owner`, `:maintainer`, `:contributor` or `:translator`, `:owner` when unset; `:owners`, `:maintainers`, `:contributors` and `:translators` list the authors of each role.

`:skip` names rules that do not apply to a repository.

GitHub shows the `SECURITY.md`, `CODE_OF_CONDUCT.md` and `CONTRIBUTING.md` of an organization's public `.github` repository for every repository of the organization that has none of its own. `:health-files-from` names that repository: where it is set, `no-health-files` deletes the three files and `security`, `code-of-conduct` and `contributing` do not apply. The example sets it for every repository of `metio` and unsets it for `metio/.github`, so that one repository carries the files everyone else falls back to. The defaults appear only on GitHub, not in a clone or a source archive.

## Rules

A rule is an EDN file named after the rule. krei looks for it in `:rule-dirs` first, then among the rules it ships:

| Rule | File | Needs |
|---|---|---|
| `license` | `LICENSE`, the text of the only license in `LICENSES/` | `:license` |
| `no-golangci` | no `.golangci.yml`, `.golangci.yaml`, `.golangci.toml` or `.golangci.json` | |
| `citation` | `CITATION.cff` with title, authors, homepage, license and keywords | `:license`, `:authors` |
| `authors` | `AUTHORS.md`, the authors by role | `:license`, `:authors` |
| `mailmap` | `.mailmap`, each of `:old-emails` mapped to the current address | `:license`, `:authors` with `:email` |
| `security` | `SECURITY.md`, where to report a vulnerability | `:license`, `:security-email` |
| `code-of-conduct` | `CODE_OF_CONDUCT.md`, the Contributor Covenant 2.0 | `:license`, `:conduct-email` |
| `contributing` | `CONTRIBUTING.md`, asking for a DCO sign-off | `:license` |
| `no-health-files` | no `SECURITY.md`, `CODE_OF_CONDUCT.md` or `CONTRIBUTING.md` | `:health-files-from` |
| `editorconfig` | `.editorconfig` with UTF-8, four spaces and a final newline | `:license` |
| `gitattributes` | `.gitattributes` normalizing the line endings of text files | `:license` |
| `go-sum-license` | `go.sum.license` declaring the license of `go.sum` | `:license`, `:go` |

```clojure
{:doc "Every repository carries the shared editorconfig."
 :when [:copyright]
 :unless [:custom-editorconfig]
 :files {".editorconfig" {:template "editorconfig"}
         "LICENSE" {:copy "LICENSES/{{license}}.txt"}
         "NOTICE" {:text "Copyright {{copyright}}\n"}
         ".travis.yml" :absent}}
```

A rule applies when every fact in `:when` is set and none in `:unless` is. Each entry of `:files` maps a path to its content:

- `{:copy path}` copies a file of the repository itself;
- `{:template path}` reads a file next to the rule;
- `{:text string}` is the content itself;
- `:absent` deletes the file.

Paths, templates and text are [Selmer](https://github.com/yogthos/Selmer) templates over the facts, so `{{license}}` is a fact, `{{a.email}}` a key of one, and `{% if %}` and `{% for %}` work as in Django. A `{{value}}` naming a fact that is not set is an error, so a rule never writes a half-filled file; `{% if %}` and `{% for %}` treat it as empty, and `{% if keywords|not-empty %}` tells an empty list from a full one. Nothing is escaped. Two rules that want different contents for one path are an error as well, and no path may leave the repository.

## Development

The `.breto.edn` names the `metio/toolchains` groups that pin babashka and the lint tools CI runs. With that registry under the `:registries` of the [breto](https://github.com/metio/breto) config, `breto env` links them into the project:

```shell
eval "$(breto env)"
bb test
```

## License

CC0-1.0, see [LICENSES/CC0-1.0.txt](LICENSES/CC0-1.0.txt).
