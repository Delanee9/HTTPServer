import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * Base class for all servlets to extend.
 */
@SuppressWarnings("serial")
abstract class BaseServlet extends HttpServlet {

    private static final boolean DEBUG = true;

    final Logger logger = Logger.getLogger(getClass().getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if(DEBUG) {
            doPost(req, resp);
        } else {
            super.doGet(req, resp);
        }
    }

    /**
     * Retrieve parameter from http request
     *
     * @param req       HttpServletRequest
     * @param parameter String
     * @return String
     * @throws ServletException
     */
    protected String getParameter(HttpServletRequest req, String parameter) throws ServletException {
        String value = req.getParameter(parameter);
        if(isEmptyOrNull(value)) {
            if(DEBUG) {
                StringBuilder parameters = new StringBuilder();
                @SuppressWarnings("unchecked")
                Enumeration<String> names = req.getParameterNames();
                while(names.hasMoreElements()) {
                    String name = names.nextElement();
                    String param = req.getParameter(name);
                    parameters.append(name).append("=").append(param).append("\n");
                }
                logger.fine("parameters: " + parameters);
            }
            throw new ServletException("Parameter " + parameter + " not found");
        }
        return value.trim();
    }

    /**
     * Set the HTTP response code
     *
     * @param resp HttpServletResponse
     */
    void setSuccess(HttpServletResponse resp) {
        setSuccess(resp, 0);
    }

    /**
     * Set the HTTP response code
     *
     * @param resp HttpServletResponse
     * @param size int
     */
    void setSuccess(HttpServletResponse resp, int size) {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.setContentLength(size);
    }

    /**
     * Checks if a string value is null or empty
     *
     * @param value String
     * @return boolean
     */
    boolean isEmptyOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }
}