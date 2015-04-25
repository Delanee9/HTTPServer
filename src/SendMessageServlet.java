import com.google.android.gcm.server.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Servlet that sends a message to a device.
 * <p/>
 * This servlet is invoked by AppEngine's Push Queue mechanism.
 */
@SuppressWarnings("serial")
public class SendMessageServlet extends BaseServlet {

    private static final String HEADER_QUEUE_COUNT = "X-AppEngine-TaskRetryCount";
    private static final String HEADER_QUEUE_NAME = "X-AppEngine-QueueName";
    private static final int MAX_RETRY = 3;

    private static final String MOBILE_NUMBER = "mobile";
    private static final String LOCATION = "location";
    private static final String PARAMETER_MULTICAST = "multicastKey";

    private Sender sender;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        sender = newSender(config);
    }

    /**
     * Creates the {@link Sender} based on the servlet settings.
     */
    Sender newSender(ServletConfig config) {
        String key = (String) config.getServletContext().getAttribute(ApiInitialiser.ATTRIBUTE_ACCESS_KEY);
        return new Sender(key);
    }

    /**
     * Notifies the App Engine that this task should be retried.
     */
    private void retryTask(HttpServletResponse resp) {
        resp.setStatus(500);
    }

    /**
     * Notifies the App Engine that this task has been completed.
     */
    private void taskDone(HttpServletResponse resp) {
        resp.setStatus(200);
    }

    /**
     * Processes the request to add a new message.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(req.getHeader(HEADER_QUEUE_NAME) == null) {
            throw new IOException("Missing header " + HEADER_QUEUE_NAME);
        }
        String retryCountHeader = req.getHeader(HEADER_QUEUE_COUNT);
        logger.fine("retry count: " + retryCountHeader);
        if(retryCountHeader != null) {
            int retryCount = Integer.parseInt(retryCountHeader);
            if(retryCount > MAX_RETRY) {
                logger.severe("Too many retries, dropping task");
                taskDone(resp);
                return;
            }
        }
        String multicastKey = req.getParameter(PARAMETER_MULTICAST);
        String location = req.getParameter(LOCATION);
        String from = req.getParameter(MOBILE_NUMBER);

        if(multicastKey != null) {
            sendMulticastMessage(from, location, multicastKey, resp);
            return;
        }
        logger.severe("Invalid request!");
        taskDone(resp);
    }

    private Message createMessage(String from, String location) {
        return new Message.Builder().addData(LOCATION, location).addData(MOBILE_NUMBER, from).build();
    }

    private void sendMulticastMessage(String from, String location, String multicastKey, HttpServletResponse resp) {
        List<String> regIds = Arrays.asList(multicastKey.split(","));
        Message message = createMessage(from, location);
        MulticastResult multicastResult;
        try {
            multicastResult = sender.sendNoRetry(message, regIds);
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Exception posting " + message, e);
            return;
        }
        boolean allDone = true;
        if(multicastResult.getCanonicalIds() != 0) {
            List<Result> results = multicastResult.getResults();
            for(int i = 0; i < results.size(); i++) {
                String canonicalRegId = results.get(i).getCanonicalRegistrationId();
                if(canonicalRegId != null) {
                    String regId = regIds.get(i);
                    Datastore.updateRegistration(regId, canonicalRegId);
                }
            }
        }
        if(multicastResult.getFailure() != 0) {
            List<Result> results = multicastResult.getResults();
            List<String> retriableRegIds = new ArrayList<>();
            for(int i = 0; i < results.size(); i++) {
                String error = results.get(i).getErrorCodeName();
                if(error != null) {
                    String regId = regIds.get(i);
                    logger.warning("Got error (" + error + ") for regId " + regId);
                    if(error.equals(Constants.ERROR_NOT_REGISTERED)) {
                        Datastore.unregister(regId);
                    }
                    if(error.equals(Constants.ERROR_UNAVAILABLE)) {
                        retriableRegIds.add(regId);
                    }
                }
            }
            if(!retriableRegIds.isEmpty()) {
                allDone = false;
                retryTask(resp);
            }
        }
        if(!allDone) {
            retryTask(resp);
        }
    }
}