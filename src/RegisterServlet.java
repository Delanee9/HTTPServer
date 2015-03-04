import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class RegisterServlet extends BaseServlet {
    private static final String PARAMETER_REG_ID = "regId";
    private static final String PARAMETER_MOBILE = "mobile";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, PARAMETER_REG_ID);
        String mobile = getParameter(req, PARAMETER_MOBILE);
        Datastore.register(regId, mobile);
        setSuccess(resp);
    }
}