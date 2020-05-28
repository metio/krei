/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

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
