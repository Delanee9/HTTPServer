import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.FilterOperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Simple implementation of a data store using standard Java collections.
 * <p/>
 * This class is neither persistent (it will lost the data when the app is
 * restarted) nor thread safe.
 */
final class Datastore {

    private static final int MULTICAST_SIZE = 1000;
    private static final String DEVICE_TYPE = "Device";
    private static final String DEVICE_REG_ID_PROPERTY = "regId";
    private static final String DEVICE_MOBILE_PROPERTY = "mobile";
    private static final String DEVICE_LOCATION_PROPERTY = "location";

    private static final String MULTICAST_TYPE = "Multicast";
    private static final String MULTICAST_REG_IDS_PROPERTY = "regIds";

    private static final FetchOptions DEFAULT_FETCH_OPTIONS = FetchOptions.Builder.withPrefetchSize(MULTICAST_SIZE).chunkSize(MULTICAST_SIZE);

    private static final Logger logger = Logger.getLogger(Datastore.class.getName());
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private Datastore() {
        throw new UnsupportedOperationException();
    }

    /**
     * Registers a device.
     *
     * @param regId device's registration id.
     */
    public static void register(String regId, String mobile) {
        logger.info("Registering " + regId);
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity entity = findDeviceByRegId(regId);
            if(entity != null) {
                logger.fine(regId + " is already registered; ignoring.");
                return;
            }
            entity = new Entity(DEVICE_TYPE, regId);
            entity.setProperty(DEVICE_MOBILE_PROPERTY, mobile);
            datastore.put(entity);
            transaction.commit();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Unregisters a device.
     *
     * @param regId device's registration id.
     */
    public static void unregister(String regId) {
        logger.info("Unregistering " + regId);
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity entity = findDeviceByRegId(regId);
            if(entity == null) {
                logger.warning("Device " + regId + " already unregistered");
            } else {
                Key key = entity.getKey();
                datastore.delete(key);
            }
            transaction.commit();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Updates the registration id of a device.
     */
    public static void updateRegistration(String oldId, String newId) {
        logger.info("Updating " + oldId + " to " + newId);
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity entity = findDeviceByRegId(oldId);
            if(entity == null) {
                logger.warning("No device for registration id " + oldId);
                return;
            }
            entity.setProperty(DEVICE_REG_ID_PROPERTY, newId);
            datastore.put(entity);
            transaction.commit();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Gets all registered devices.
     */
    public static List<String> getDevices() {
        List<String> devices;
        Transaction transaction = datastore.beginTransaction();
        try {
            Query query = new Query(DEVICE_TYPE);
            Iterable<Entity> entities = datastore.prepare(query).asIterable(DEFAULT_FETCH_OPTIONS);
            devices = new ArrayList<>();
            for(Entity entity : entities) {
                String device = (String) entity.getProperty(DEVICE_REG_ID_PROPERTY);
                devices.add(device);
            }
            transaction.commit();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
        return devices;
    }

    /**
     * Gets the number of total devices.
     */
    public static int getTotalDevices() {
        Transaction transaction = datastore.beginTransaction();
        try {
            Query query = new Query(DEVICE_TYPE).setKeysOnly();
            List<Entity> allKeys =
                    datastore.prepare(query).asList(DEFAULT_FETCH_OPTIONS);
            int total = allKeys.size();
            logger.fine("Total number of devices: " + total);
            transaction.commit();
            return total;
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    private static Entity findDeviceByRegId(String regId) {
        Query.Filter propertyFilter = new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, KeyFactory.createKey(DEVICE_TYPE, regId));
        Query query = new Query(DEVICE_TYPE).setFilter(propertyFilter);
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
        Entity entity = null;
        if(!entities.isEmpty()) {
            entity = entities.get(0);
        }
        int size = entities.size();
        if(size > 0) {
            logger.severe("Found " + size + " entities for regId " + regId + ": " + entities);
        }
        return entity;
    }

    /**
     * Creates a persistent record with the devices to be notified using a
     * multicast message.
     *
     * @param devices registration ids of the devices.
     * @return encoded key for the persistent record.
     */
    public static String createMulticast(List<String> devices) {
        logger.info("Stconfigoring multicast for " + devices.size() + " devices");
        String encodedKey;
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity entity = new Entity(MULTICAST_TYPE);
            entity.setProperty(MULTICAST_REG_IDS_PROPERTY, devices);
            datastore.put(entity);
            Key key = entity.getKey();
            encodedKey = KeyFactory.keyToString(key);
            logger.fine("multicast key: " + encodedKey);
            transaction.commit();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
        return encodedKey;
    }

    /**
     * Gets a persistent record with the devices to be notified using a
     * multicast message.
     * pp engine
     *
     * @param encodedKey encoded key for the persistent record.
     */
    public static List<String> getMulticast(String encodedKey) {
        Key key = KeyFactory.stringToKey(encodedKey);
        Entity entity;
        Transaction transaction = datastore.beginTransaction();
        try {
            entity = datastore.get(key);
            @SuppressWarnings("unchecked")
            List<String> devices = (List<String>) entity.getProperty(MULTICAST_REG_IDS_PROPERTY);
            transaction.commit();
            return devices;
        } catch(EntityNotFoundException e) {
            logger.severe("No entity for key " + key);
            return Collections.emptyList();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Updates a persistent record with the devices to be notified using a
     * multicast message.
     *
     * @param encodedKey encoded key for the persistent record.
     * @param devices    new list of registration ids of the devices.
     */
    public static void updateMulticast(String encodedKey, List<String> devices) {
        Key key = KeyFactory.stringToKey(encodedKey);
        Entity entity;
        Transaction transaction = datastore.beginTransaction();
        try {
            try {
                entity = datastore.get(key);
            } catch(EntityNotFoundException e) {
                logger.severe("No entity for key " + key);
                return;
            }
            entity.setProperty(MULTICAST_REG_IDS_PROPERTY, devices);
            datastore.put(entity);
            transaction.commit();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Deletes a persistent record with the devices to be notified using a
     * multicast message.
     *
     * @param encodedKey encoded key for the persistent record.
     */
    public static void deleteMulticast(String encodedKey) {
        Transaction transaction = datastore.beginTransaction();
        try {
            Key key = KeyFactory.stringToKey(encodedKey);
            datastore.delete(key);
            transaction.commit();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    public static void updateLocation(String regId, String location) {
        logger.info("Update Location " + regId);
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity entity = findDeviceByRegId(regId);
            if(entity == null) {
                logger.warning("No device for registration id " + regId);
                return;
            }
            entity.setProperty(DEVICE_LOCATION_PROPERTY, location);
            datastore.put(entity);
            transaction.commit();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    public static List<String> proximityCheck(String regId, String latitude, String longitude) {
        Query.Filter propertyFilter = new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, KeyFactory.createKey(DEVICE_TYPE, regId));
        Query query = new Query(DEVICE_TYPE).setFilter(propertyFilter);
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
        Entity entity = null;
        if(!entities.isEmpty()) {
            entity = entities.get(0);
        }
        int size = entities.size();
        if(size > 0) {
            logger.severe("Found " + size + " entities for regId " + regId + ": " + entities);
        }
        return null;
    }
}