import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class that allows a user to become visible to friends in close proximity.
 */
@SuppressWarnings("serial")
public class VisibilityServlet extends BaseServlet {
    private static final String PARAMETER_REG_ID = "regId";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, PARAMETER_REG_ID);
        String [] friends = getParameter(req, PARAMETER_REG_ID).split("-");

        setSuccess(resp);
    }
}