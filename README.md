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
krei apply DIR REPO     # write the changes of one repository into the working tree DIR
```

`REPO` is a glob over repository names or `host/path` ids, such as `aro` or `github.com/metio/*`. Without one, every managed repository is used, and a `REPO` that matches none is an error. The config is read from `$XDG_CONFIG_HOME/krei/config.edn` unless `--config` names another file.

Each command works on a clone below `$XDG_CACHE_HOME/krei/<host>/<path>`, fetched and reset to the default branch on every run, so local clones elsewhere are never touched. `krei sync` commits onto the branch `krei/sync`, started from the default branch, and force-pushes it. When the branch on the remote already holds the same files, nothing is pushed. On `github.com` it then opens a pull request from that branch unless one is open already; on other hosts, or when `gh` fails, for example because it is not logged in, it pushes the branch and leaves the pull request to you, naming the reason `gh` gave. Commits are signed off and follow your git config, including commit signing.

`krei apply` writes into a working tree of your own instead, reading the facts from it, and leaves everything else in it as it is. Use it when a change needs edits of its own beside the rule output, such as a `pom.xml` moving to a new parent in the same commit that replaces the workflows depending on the old one.

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
         "github.com/metio/.github" {:health-files-from nil :copyright "The metio Authors"
                                     :skip [:citation :authors :mailmap]}
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
2. facts read from the checkout: `:licenses` lists the identifiers of the files in `LICENSES/`, and `:license` is the only one, unset when there are several or none; the facts in the table below are true when one of their files is present;
3. `:facts` under `"*"`;
4. `:facts` under glob patterns, from the least to the most specific;
5. `:facts` under the exact repository id;
6. the repository's own `.krei.edn`, a map of facts kept next to the files they shape.

Maps merge key by key, so `{:typos {:words ["edn"]}}` under `"*"` and `{:typos {:exclude ["vendor/"]}}` for one repository give that repository both; any other value replaces the one before it.

| Fact | Files |
|---|---|
| `:go` | `go.mod` |
| `:maven` | `pom.xml` |
| `:cargo` | `Cargo.toml` |
| `:npm` | `package.json` |
| `:babashka` | `bb.edn` |
| `:clojure` | `deps.edn` |
| `:nix` | `flake.nix`, `shell.nix` |
| `:breto` | `.breto.edn` |
| `:container` | `Dockerfile`, `Containerfile` |
| `:helm` | `Chart.yaml`, `charts/*/Chart.yaml` |
| `:hugo` | `docs/hugo.toml`, `docs/hugo.yaml`, `docs/config.toml`, `docs/config.yaml` |
| `:terraform-provider` | `terraform-registry-manifest.json` |
| `:reuse-toml` | `REUSE.toml` |
| `:vale` | `.vale.ini` |

Derived facts fill in whatever none of these set: `:title` is the `:name`, `:copyright` is `The <title> Authors`, `:homepage` is `https://<host>/<path>/`, and `:keywords` are the `:topics`. Each of `:authors` is a map of `:given-names`, `:family-names`, an optional `:email`, `:old-emails` and a `:role` of `:owner`, `:maintainer`, `:contributor` or `:translator`, `:owner` when unset; `:owners`, `:maintainers`, `:contributors` and `:translators` list the authors of each role.

`:skip` names rules that do not apply to a repository.

`:ci` pins the metio/ci reusable workflows the generated workflows call, as `{:ref "<commit>" :version "<tag>"}`. `:maven-central` marks a repository that releases to Maven Central: `:day` is its day of the month, `:paths` the paths whose changes warrant a release, and `:deploy-args` optional extra arguments for the deploy. `:maven-verify` opts a Maven repository into the generated `verify.yml`; its optional `:goals` and `:maven-args` are passed to the pipeline.

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
| `license-texts` | each file in `LICENSES/` holds the SPDX reference text, for the licenses of license-list-data v3.29.0 krei ships | `:licenses` |
| `renovate` | `renovate.json5` printing `:renovate` as JSON, and no `renovate.json` | `:license`, `:renovate` |
| `no-dependabot` | no `.github/dependabot.yml` or Dependabot auto-merge workflow | `:renovate` |
| `markdownlint` | `.markdownlint.yaml`, plus the rules in `:markdownlint :disable` turned off | `:license` |
| `yamllint` | `.yamllint.yaml`, ignoring `docs/public/` for Hugo, `node_modules/` for npm and the paths in `:yamllint :ignore` | `:license` |
| `typos` | `.typos.toml` with the words in `:typos :words` and `:identifiers`, the patterns in `:ignore-re` and the paths in `:exclude` | `:license` |
| `vale` | `.vale.ini` reading the released metio/vale-config package | `:license`, `:vale` |
| `gitignore` | `.gitignore` with a section per detected tool and the lines in `:gitignore` | `:license` |
| `no-intellij` | no file below `.idea/` | |
| `no-codeql` | no CodeQL workflow | |
| `no-dep5` | no `.reuse/dep5` | `:reuse-toml` |
| `maven-central-release` | `.github/workflows/release.yml`, a monthly gated release to Maven Central | `:license`, `:maven`, `:breto`, `:maven-central`, `:ci` |
| `maven-verify` | `.github/workflows/verify.yml`, the Maven pipeline, REUSE and actionlint behind one `Verify` check | `:license`, `:maven`, `:breto`, `:maven-verify`, `:ci` |
| `main-branch` | checks that the default branch is `main` and that no workflow branch filter, `refs/heads/` ref or link to the repository's own files names `master` | |
| `no-legacy-java-ci` | no `java.properties`, `managed-java`/`managed-maven` actions, `update-parent.yml` or `reuse.yml` | `:maven-verify` |

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
- `{:template path}` reads a file next to the rule; `:preserve` lists regexes whose matches keep the text the existing file has for the same first group, so `["uses: ([^@\\s]+)@[0-9a-f]{40}(?: # \\S+)?"]` leaves every pinned action as Renovate last bumped it while the rest follows the template, and only a new file takes the template's pins;
- `{:verbatim path}` reads a file next to the rule and writes it unrendered; with `:optional true`, a path the rule holds no file for is left alone;
- `{:text string}` is the content itself;
- `:absent` deletes the file; a path holding `*`, `?`, `[` or `{` deletes every file the glob matches.

A rule can also check what it cannot fix. `:expect` maps facts to the values they must have, and `:forbid` maps a glob to regexes no file it selects may match. `plan` and `check` report each breach as drift, naming the file and line, and `sync` reports it without changing anything, since a default branch or a hand-written workflow needs a decision of its own.

`:each` names a fact holding a list, and the rule's files are then rendered once per element, which templates read as `{{each}}`. A copy reads what the other rules write in the same run, so `LICENSE` follows a `LICENSES/` file another rule rewrites.

Paths, copy sources, templates, verbatim paths and text are [Selmer](https://github.com/yogthos/Selmer) templates over the facts, so `{{license}}` is a fact, `{{a.email}}` a key of one, and `{% if %}` and `{% for %}` work as in Django. A `{{value}}` naming a fact that is not set is an error, so a rule never writes a half-filled file; `{% if %}` and `{% for %}` treat it as empty, and `{% if keywords|not-empty %}` tells an empty list from a full one. The `json` filter prints a fact as JSON with sorted keys. Nothing is escaped. Two rules that want different contents for one path are an error as well, and no path may leave the repository.

## Development

The `.breto.edn` names the `metio/toolchains` groups that pin babashka and the lint tools CI runs. With that registry under the `:registries` of the [breto](https://github.com/metio/breto) config, `breto env` links them into the project:

```shell
eval "$(breto env)"
bb test
```

## License

CC0-1.0, see [LICENSES/CC0-1.0.txt](LICENSES/CC0-1.0.txt).
