package wtf.metio.krei.construct.metio;

import wtf.metio.krei.model.ide.IDE;
import wtf.metio.krei.model.ide.idea.Idea;
import wtf.metio.krei.model.ide.idea.IdeaCodeStyle;
import wtf.metio.krei.model.ide.idea.IdeaCopyright;

public final class MetioIdea {

    public static IDE configure() {
        return Idea.builder()
                .codeStyle(IdeaCodeStyle.of())
                .copyright(IdeaCopyright.of())
                .build();
    }

    private MetioIdea() {
        // factory class
    }

}
