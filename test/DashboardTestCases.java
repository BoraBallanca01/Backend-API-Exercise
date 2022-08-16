import com.fasterxml.jackson.databind.JsonNode;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.utils.DatabaseUtils;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class DashboardTestCases extends WithApplication {

    String token;

    @Before
    public void setup() {
        User user = new User("bora", "password", new ArrayList<>());
        user.setId(new ObjectId("62ea83e3f652567bf36e9735"));
        final Http.RequestBuilder userRequest = new Http.RequestBuilder()
                .method("POST")
                .uri("/api/user/")
                .bodyJson(Json.toJson(user));
        route(app, userRequest);

        final Http.RequestBuilder authenticateRequest = new Http.RequestBuilder()
                .method("POST")
                .uri("/api/authenticate/")
                .bodyJson(Json.toJson(user));
        final Result authenticate = route(app, authenticateRequest);

        assertEquals(OK, authenticate.status());
        JsonNode node = Json.parse(contentAsString(authenticate));
        token = Json.fromJson(node, String.class);
        Dashboard dashboard1 = new Dashboard("Tools",
                "enable inovative channels",
                null,
                Arrays.asList("62ea83e3f652567bf36e9735"),
                Arrays.asList("62ea83e3f652567bf36e9735"),
                0,
                null,
                null
        );
        dashboard1.setId(new ObjectId("62ea320afc13ae31a1000136"));

        final Http.RequestBuilder dashboardRequest = new Http.RequestBuilder()
                .method("POST")
                .uri("/api/dashboard/")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard1));
        final Result result = route(app, dashboardRequest);
        assertEquals(OK, result.status());
    }

    @Test
    public void createTest() {
        Dashboard dashboard1 = new Dashboard("Whatever",
                "enable inovative channels",
                null,
                Arrays.asList("62ea83e3f652567bf36e9735"),
                Arrays.asList("62ea83e3f652567bf36e9735"),
                0,
                new ArrayList<>(),
                new ArrayList<>()
        );
        dashboard1.setId(new ObjectId("62ea320afc13ae31a1000137"));

        final Http.RequestBuilder dashboardRequest = new Http.RequestBuilder()
                .method("POST")
                .uri("/api/dashboard/")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard1));
        final Result result = route(app, dashboardRequest);
        assertEquals(OK, result.status());
        JsonNode node = Json.parse(contentAsString(result));
        Dashboard finalDashboard = Json.fromJson(node, Dashboard.class);
        assertEquals(dashboard1, finalDashboard);
    }

    @Test
    public void createBadDashboardTest() {
        Dashboard dashboard1 = new Dashboard(null,
                null,
                null,
                Arrays.asList("62ea83e3f652567bf36e9735"),
                Arrays.asList("62ea83e3f652567bf36e9735"),
                0,
                new ArrayList<>(),
                new ArrayList<>()
        );
        dashboard1.setId(new ObjectId());

        final Http.RequestBuilder dashboardRequest = new Http.RequestBuilder()
                .method("POST")
                .uri("/api/dashboard/")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard1));
        final Result result = route(app, dashboardRequest);
        assertEquals(BAD_REQUEST, result.status());
    }


    @Test
    public void readTest() {
        final Http.RequestBuilder readRequest = new Http.RequestBuilder()
                .method("GET")
                .uri("/api/dashboard/")
                .header("token", token)
                .bodyJson(Json.toJson(""));
        final Result result = route(app, readRequest);
        assertEquals(OK, result.status());

    }

    @Test
    public void updateTest() {
        Dashboard dashboard = new Dashboard("Beauty",
                "maximize vertical supply-chains",
                null,
                Arrays.asList("62ea83e3f652567bf36e9735"),
                Arrays.asList("62ea83e3f652567bf36e9735"),
                0,
                new ArrayList<>(),
                new ArrayList<>()
        );
        final Http.RequestBuilder updateRequest = new Http.RequestBuilder()
                .method("PUT")
                .uri("/api/dashboard/62ea320afc13ae31a1000136")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard));
        final Result result = route(app, updateRequest);
        assertEquals(OK, result.status());
        JsonNode body = Json.parse(contentAsString(result));
        Dashboard resultDashboard = Json.fromJson(body, Dashboard.class);
        assertEquals(resultDashboard, dashboard);
    }

    @Test
    public void updateNoDashboardTest() {
        Dashboard dashboard = new Dashboard();
        final Http.RequestBuilder updateRequest = new Http.RequestBuilder()
                .method("PUT")
                .uri("/api/dashboard/62ea320afc13ae31a1000136")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard));
        final Result result = route(app, updateRequest);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void deleteTest() {
        Dashboard dashboard = new Dashboard("Beauty",
                "maximize vertical supply-chains",
                null,
                Arrays.asList("62ea83e3f652567bf36e9735"),
                Arrays.asList("62ea83e3f652567bf36e9735"),
                0,
                new ArrayList<>(),
                new ArrayList<>()
        );
        final Http.RequestBuilder updateRequest = new Http.RequestBuilder()
                .method("DELETE")
                .uri("/api/dashboard/62ea320afc13ae31a1000136")
                .header("token", token)
                .bodyJson(Json.toJson(dashboard));
        final Result result = route(app, updateRequest);
        assertEquals(OK, result.status());

    }

}