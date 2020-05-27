package wtf.metio.krei.model.build.maven;

import org.immutables.value.Value;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Configuration of a Maven parent.
 */
@Value.Immutable
public interface MavenDependency {

    MavenGAV gav();

    Optional<String> scope();

}