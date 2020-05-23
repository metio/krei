package wtf.metio.krei.template.intellij;

public final class Copyright {

    public static final String TEXT = """
            <component name="CopyrightManager">
              <copyright>
                <option name="keyword" value="%s" />
                <option name="notice" value="%s" />
                <option name="myName" value="%s" />
              </copyright>
            </component>""";

    private Copyright() {
        // constants class
    }

}
