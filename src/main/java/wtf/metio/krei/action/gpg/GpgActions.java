/*
 * This file is part of krei. It is subject to the license terms in the LICENSE file found in the top-level
 * directory of this distribution and at http://creativecommons.org/publicdomain/zero/1.0/. No part of krei,
 * including this file, may be copied, modified, propagated, or distributed except according to the terms contained
 * in the LICENSE file.
 */

package wtf.metio.krei.action.gpg;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.model.Action;
import wtf.metio.krei.template.Templates;

import java.nio.file.Path;
import java.security.Security;

public final class GpgActions {

    public static Action sign(final Path file) {
        Security.addProvider(new BouncyCastleProvider());
        final var content = Templates.template("gitignore", file + ".gitignore");
        return FileActions.appendFile(null, content);
    }

    private GpgActions() {
        // factory class
    }

}
