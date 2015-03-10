import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Register a new user and save the information in the database.
 */
@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet {

    private static final String REGISTRATION_ID = "regId";
    private static final String MOBILE_NUMBER = "mobile";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, REGISTRATION_ID);
        String mobile = getParameter(req, MOBILE_NUMBER);
        Datastore.register(regId, mobile);
        setSuccess(resp);
    }
}