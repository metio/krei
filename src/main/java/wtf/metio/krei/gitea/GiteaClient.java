package wtf.metio.krei.gitea;

public interface GiteaClient {

    //region Organization
    int createRepositoryInOrganization(String projectName, String organization);
    //endregion

    //region Repository
    int createLabel(String projectName, String organization, CreateLabelOption label);
    //endregion

}
