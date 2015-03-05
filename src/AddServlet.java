import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds a new friend or list of friends to a Users friend list
 */
public class AddServlet extends BaseServlet {

    private static final String PARAMETER_REG_ID = "regId";
    private static final String PARAMETER_FRIENDS_LIST = "friendsList";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, PARAMETER_REG_ID);
        String friendsList = getParameter(req, PARAMETER_FRIENDS_LIST);
        Datastore.addFriends(regId, friendsList);
        setSuccess(resp);
    }
}