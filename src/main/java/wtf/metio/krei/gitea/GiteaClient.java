package wtf.metio.krei.gitea;

public interface GiteaClient {

    int createRepositoryInOrganization(String projectName, String organization);

    int createLabel(String projectName, String organization, CreateLabelOption label);

}
