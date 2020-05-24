package wtf.metio.krei.action.gpg;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import wtf.metio.krei.action.file.FileActions;
import wtf.metio.krei.io.Templates;
import wtf.metio.krei.model.Action;

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
