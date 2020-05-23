package wtf.metio.krei.template.gitignore;

public final class Maven {

    public static final String TEXT = """
            # gitignore template for Maven build system
            # website: https://maven.apache.org/
                        
            target/
            pom.xml.tag
            pom.xml.releaseBackup
            pom.xml.versionsBackup
            pom.xml.next
            release.properties
            dependency-reduced-pom.xml
            buildNumber.properties
            .mvn/timing.properties
            .mvn/wrapper/maven-wrapper.jar""";

    private Maven() {
        // constants class
    }

}
