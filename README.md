# krei

Project creation & setup tool.

## Ideas

- scipts that produce part of a project
- mixins or rather includes? Could use special markers like <!-- krei-managed: unit-id --> in a pom.xml

### systemd

#### search path

Adapt for krei

Units can be aliased (have an alternative name), by creating a symlink from the new name to the existing name in one of the unit search paths.

```
/etc/systemd/system.control/*
/run/systemd/system.control/*
/run/systemd/transient/*
/run/systemd/generator.early/*
/etc/systemd/system/*
/etc/systemd/systemd.attached/*
/run/systemd/system/*
/run/systemd/systemd.attached/*
/run/systemd/generator/*
…
/usr/lib/systemd/system/*
/run/systemd/generator.late/*
User Unit Search Path

~/.config/systemd/user.control/*
$XDG_RUNTIME_DIR/systemd/user.control/*
$XDG_RUNTIME_DIR/systemd/transient/*
$XDG_RUNTIME_DIR/systemd/generator.early/*
~/.config/systemd/user/*
/etc/systemd/user/*
$XDG_RUNTIME_DIR/systemd/user/*
/run/systemd/user/*
$XDG_RUNTIME_DIR/systemd/generator/*
~/.local/share/systemd/user/*
…
/usr/lib/systemd/user/*
$XDG_RUNTIME_DIR/systemd/generator.late/*
```

## Usage

TBD

## Alternatives

- TBD

## License

CC0

## Mirrors

- TBD
