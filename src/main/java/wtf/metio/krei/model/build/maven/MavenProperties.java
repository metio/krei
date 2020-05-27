package wtf.metio.krei.model.build.maven;

import org.immutables.value.Value;

import java.util.Optional;

/**
 * Configuration of Maven properties.
 */
@Value.Immutable
public interface MavenProperties {

    Optional<String> encoding();

}
