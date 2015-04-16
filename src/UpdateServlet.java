import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Updates a users location and saves the changes to the database.
 */
@SuppressWarnings("serial")
public class UpdateServlet extends BaseServlet {

    private static final String REGISTRATION_ID = "regId";
    private static final String LOCATION = "location";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, REGISTRATION_ID);
        String location = getParameter(req, LOCATION);
        Datastore.updateLocation(regId, location);
        Datastore.proximityCheck(regId);
        setSuccess(resp);
    }
}