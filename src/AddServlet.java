import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Adds a new friend or list of friends to a Users friend list
 */
@SuppressWarnings("serial")
public class AddServlet extends BaseServlet {

    private static final String PARAMETER_REG_ID = "regId";
    private static final String PARAMETER_FRIENDS_LIST = "friendsList";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, PARAMETER_REG_ID);
        HashSet<String> friendsList = new HashSet<>(Arrays.asList(getParameter(req, PARAMETER_FRIENDS_LIST).split(",")));
        Datastore.addFriends(regId, friendsList);
        setSuccess(resp);
    }
}