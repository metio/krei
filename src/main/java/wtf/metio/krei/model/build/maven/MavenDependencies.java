package wtf.metio.krei.model.build.maven;

import org.immutables.value.Value;

import java.util.List;

/**
 * Configuration of Maven dependencies.
 */
@Value.Immutable
public interface MavenDependencies {

    List<MavenDependency> dependency();

    List<MavenDependency> dependencyManagement();

}
