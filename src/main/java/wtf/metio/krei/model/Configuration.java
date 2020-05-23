package wtf.metio.krei.model;

import java.nio.file.Path;

public record Configuration(Path projectDirectory, String projectName) {
}
