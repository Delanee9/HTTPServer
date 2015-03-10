import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Removes a friend or a list of friends from a Users friend list.
 */
@SuppressWarnings("serial")
public class RemoveServlet extends BaseServlet {

    private static final String REGISTRATION_ID = "regId";
    private static final String FRIENDS_LIST = "friendsList";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        String regId = getParameter(req, REGISTRATION_ID);
        HashSet<String> friendsList = new HashSet<>(Arrays.asList(getParameter(req, FRIENDS_LIST).split(",")));
        Datastore.removeFriends(regId, friendsList);
        setSuccess(resp);
    }
}