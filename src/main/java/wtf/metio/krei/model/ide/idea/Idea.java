package wtf.metio.krei.model.ide.idea;

import org.immutables.value.Value;
import wtf.metio.krei.model.ide.IDE;

import java.util.Optional;

/**
 * Configuration of Jetbrains IntelliJ IDEA
 *
 * @see <a href="https://www.jetbrains.com/idea/">jetbrains.com/idea</a>
 */
@Value.Immutable
public interface Idea extends IDE {

    //region Builders
    static ImmutableIdea.Builder builder() {
        return ImmutableIdea.builder();
    }
    //endregion

    Optional<IdeaCopyright> copyright();

    Optional<IdeaCodeStyle> codeStyle();

}
