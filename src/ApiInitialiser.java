import com.google.appengine.api.datastore.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

class ApiInitialiser implements ServletContextListener {

    static final String ATTRIBUTE_ACCESS_KEY = "apiKey";
    private static final String ENTITY_KIND = "Settings";
    private static final String ENTITY_KEY = "MyKey";
    private static final String ACCESS_KEY_FIELD = "ApiKey";

    private final Logger logger = Logger.getLogger(getClass().getName());

    public void contextInitialized(ServletContextEvent event) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey(ENTITY_KIND, ENTITY_KEY);

        Entity entity;
        try {
            entity = datastore.get(key);
        } catch(EntityNotFoundException e) {
            entity = new Entity(key);
            entity.setProperty(ACCESS_KEY_FIELD, "AIzaSyBh_4TN3jujVl9PaCpQfhHkqBzKlNIDeRQ");
            datastore.put(entity);
            logger.severe("Created fake key. Please go to App Engine admin " + "console, change its value to your API Key (the entity " + "type is '" + ENTITY_KIND + "' and its field to be changed is '" + ACCESS_KEY_FIELD + "'), then restart the server!");
        }
        String accessKey = (String) entity.getProperty(ACCESS_KEY_FIELD);
        event.getServletContext().setAttribute(ATTRIBUTE_ACCESS_KEY, accessKey);
    }

    public void contextDestroyed(ServletContextEvent event) {
    }
}