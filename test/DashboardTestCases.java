import com.fasterxml.jackson.databind.JsonNode;
import io.exercise.api.models.AuthenticatedUser;
import io.exercise.api.models.Dashboard;
import org.bson.types.ObjectId;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import static play.mvc.Http.Status.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class DashboardTestCases extends WithApplication {
//    public String authenticateUser()
//    {
//        AuthenticatedUser user=new AuthenticatedUser("bora","bora");
//        user.
//        return null;
//
//    }


    @Test
    public void createTest() {
        Dashboard dashboard = new Dashboard(
                "Tools",
                "e-enable innovative channels",
                null,
                Arrays.asList("62ea2bbc21982230a035bdc0", "62ea2bab12d9a44c9e2c1485"),
                Arrays.asList("62ea2bc0b826c8c660a552e6", "62e90a944e9bf2171ca6df26"),
                0,
                null,
                null
        );
        dashboard.setId(new ObjectId());
        final Http.RequestBuilder homeRequest=new Http.RequestBuilder()
                .method("POST")
                .uri("/api/dashboard/")
                .header("token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiI2MmUyNTdmNmRiZmYzNTNmNTZlZjMwMjUifQ.oXQE0XBybPndUUGUKh2AgP6PTNjXRVIOwngho148Qpc")
                .bodyJson(Json.toJson(dashboard));

        final Result result = route(app, homeRequest);
        assertEquals(OK,result.status());

        JsonNode body=Json.parse(contentAsString(result));
        Dashboard resultDash= Json.fromJson(body,Dashboard.class);
        assertEquals(resultDash,dashboard);


    }
}
