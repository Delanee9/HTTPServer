import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Updates a users location and saves the changes to the database.
 */
@SuppressWarnings("serial")
public class UpdateServlet extends BaseServlet {

    private static final String PARAMETER_REG_ID = "regId";
    private static final String PARAMETER_LOCATION = "location";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, PARAMETER_REG_ID);
        String location = getParameter(req, PARAMETER_LOCATION);
        Datastore.updateLocation(regId, location);
        setSuccess(resp);
    }
}
