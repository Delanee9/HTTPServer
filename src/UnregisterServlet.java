import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that handles the user unregistration process.
 */
@SuppressWarnings("serial")
public class UnregisterServlet extends BaseServlet {

    private static final String REGISTRATION_ID = "regId";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, REGISTRATION_ID);
        Datastore.unregister(regId);
        setSuccess(resp);
    }
}