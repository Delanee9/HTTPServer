import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Removes a friend or a list of friends from a Users friend list.
 */
@SuppressWarnings("serial")
public class RemoveServlet extends BaseServlet {

    private static final String PARAMETER_REG_ID = "regId";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, PARAMETER_REG_ID);
        String [] friends = getParameter(req, PARAMETER_REG_ID).split("-");

        setSuccess(resp);
    }
}