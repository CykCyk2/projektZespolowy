import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import org.joda.time.DateTime;

public class JRC
{
  // final URI jiraServerUri = new URI("http://156.17.41.242:8080");
   // final JiraRestClient restClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(jiraServerUri, "adi", "Comarch2019");

    private static String user = "adi";
    private static String passsword = "Comarch2019";
    private static String jiraUrl = "http://156.17.41.242:8080";
    private static JiraRestClient restClient;

    public JRC() throws URISyntaxException, FileNotFoundException {

        URI jiraUri = URI.create(jiraUrl);
        restClient = new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(jiraUri, user, passsword);

        //Pobieranie z Jiry wszystkich projektów
        List<BasicProject> projects = new LinkedList<BasicProject>();
        for (BasicProject project : restClient.getProjectClient().getAllProjects().claim()) {
            //Wypisanie nazwy projektu
            System.out.println(project.getKey() + ": " + project.getName());
            projects.add(project);
        }

        //Dla każdego projektu pobieram liste zgłoszeń z nieprzypisanymi osobami
        for (BasicProject project : projects) {
            Promise<SearchResult> searchJqlPromise = restClient.getSearchClient().searchJql("project = " + project.getName() + " AND assignee is EMPTY ORDER BY assignee, resolutiondate");
            for (Issue issue : searchJqlPromise.claim().getIssues()) {
                //TUTAJ Mozesz dla każdego zgloszenia nieprzypisanego do uztkownika cos z nim zrobić
                System.out.println(issue.getSummary());

                //Aby przypisać uzytkownika pobierasz uztykownika
                BasicUser userToAssign = null;

                WineDemo warsztat = new WineDemo(issue.getSummary());
                String userName="";
                try {
                    userName= warsztat.updateWines();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    userToAssign = restClient.getUserClient().getUser(userName).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                addComment(issue.getKey(),"Pewnosc wboru: "+warsztat.procent +"%");


                //i go przypisujesz
                assignee(issue.getKey(), userToAssign);
            }
        }
    }

    public Issue getIssue(String issueKey) throws Exception
    {
        Promise issuePromise = restClient.getIssueClient().getIssue(issueKey);
        return Optional.ofNullable((Issue) issuePromise.claim()).orElseThrow(() -> new Exception("No such issue"));
    }

    public void assignee(String issueKey, BasicUser user) {
        IssueInput input = new IssueInputBuilder().setAssignee(user).build();
        restClient.getIssueClient()
                .updateIssue(issueKey, input)
                .claim();
    }

    public void addComment(String issueKey, String commentBody) {
        Promise issuePromise = restClient.getIssueClient().getIssue(issueKey);
        try {
            Issue tmpIssue = (Issue) issuePromise.get();
            restClient.getIssueClient()
                    .addComment(tmpIssue.getCommentsUri(), Comment.valueOf(commentBody));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }

    }
}
