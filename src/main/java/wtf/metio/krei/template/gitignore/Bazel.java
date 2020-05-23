package wtf.metio.krei.template.gitignore;

public final class Bazel {

    public static final String TEXT = """
            # gitignore template for Bazel build system
            # website: https://bazel.build/
                        
            # Ignore all bazel-* symlinks. There is no full list since this can change
            # based on the name of the directory bazel is cloned into.
            /bazel-*""";

    private Bazel() {
        // constants class
    }

}
