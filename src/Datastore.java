import com.google.appengine.api.datastore.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

/**
 * NoSQL datastore
 */
final class Datastore {

    private static final int MULTICAST_SIZE = 1000;
    private static final String DEVICE_TYPE = "Device";
    private static final String REGISTRATION_ID = "regId";
    private static final String MOBILE_NUMBER = "mobile";
    private static final String LOCATION = "location";
    private static final String FRIENDS_LIST = "friendsList";

    private static final String MULTICAST_REG_IDS_PROPERTY = "regIds";

    private static final FetchOptions DEFAULT_FETCH_OPTIONS = FetchOptions.Builder.withPrefetchSize(MULTICAST_SIZE).chunkSize(MULTICAST_SIZE);

    private static final Logger logger = Logger.getLogger(Datastore.class.getName());
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private Datastore() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the number of total devices.
     */
    public static int getTotalDevices() {
        Transaction transaction = datastore.beginTransaction();
        try {
            Query query = new Query(DEVICE_TYPE).setKeysOnly();
            List<Entity> allKeys = datastore.prepare(query).asList(DEFAULT_FETCH_OPTIONS);
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


    //-----------------------------Complete--------------------------------------------------------

    /**
     * Registers a user on the system
     *
     * @param regId  String
     * @param mobile String
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
            entity.setProperty(MOBILE_NUMBER, mobile);
            entity.setProperty(FRIENDS_LIST, new ArrayList<String>());
            entity.setProperty(LOCATION, "");
            datastore.put(entity);
            transaction.commit();
        } catch(Exception e) {
            logger.severe("Failure in register - " + e);
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
        } catch(Exception e) {
            logger.severe("Failure in unregister - " + e);
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Updates the registration id of a device.
     *
     * @param oldId String
     * @param newId String
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
            entity.setProperty(REGISTRATION_ID, newId);
            datastore.put(entity);
            transaction.commit();
        } catch(Exception e) {
            logger.severe("Failure in updateRegistration - " + e);
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Remove friends from friend list.
     *
     * @param regId       String
     * @param friendsList List<String>
     */
    @SuppressWarnings("unchecked")
    public static void removeFriends(String regId, HashSet<String> friendsList) {
        logger.info("Removing friends from " + regId);
        Transaction transaction = datastore.beginTransaction();
        ArrayList<String> list;
        try {
            Entity entity = findDeviceByRegId(regId);
            if(entity == null) {
                logger.warning("No device for registration id " + regId);
                return;
            }
            list = (ArrayList<String>) (entity.getProperty(FRIENDS_LIST));
            if(list == null) {
                list = new ArrayList<>();
            }
            list.removeAll(friendsList);

            entity.setProperty(FRIENDS_LIST, list);
            datastore.put(entity);
            transaction.commit();
            System.out.println("works");
        } catch(Exception e) {
            logger.severe("Failure in removeFriends - " + e);
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Add friends to friends list.
     *
     * @param regId       String
     * @param friendsList List<String>
     */
    @SuppressWarnings("unchecked")
    public static void addFriends(String regId, HashSet<String> friendsList) {
        logger.info("Adding friends to " + regId);
        Transaction transaction = datastore.beginTransaction();
        ArrayList<String> list;
        try {
            Entity entity = findDeviceByRegId(regId);
            if(entity == null) {
                logger.warning("No device for registration id " + regId);
                return;
            }
            list = (ArrayList<String>) (entity.getProperty(FRIENDS_LIST));
            if(list == null) {
                list = new ArrayList<>();
            }
            list.addAll(friendsList);

            entity.setProperty(FRIENDS_LIST, list);
            datastore.put(entity);
            transaction.commit();
        } catch(Exception e) {
            logger.severe("Failure in addFriends - " + e);
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Updates the user location information
     *
     * @param regId    String
     * @param location String
     */
    public static void updateLocation(String regId, String location) {
        logger.info("Update Location " + regId);
        Transaction transaction = datastore.beginTransaction();
        try {
            Entity entity = findDeviceByRegId(regId);
            if(entity == null) {
                logger.warning("No device for registration id " + regId);
                return;
            }
            entity.setProperty(LOCATION, location);
            datastore.put(entity);
            transaction.commit();
        } catch(Exception e) {
            logger.severe("Failure in updateLocation - " + e);
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Check the proximity of friends and return a list of close friends.
     *
     * @param regId   String
     * @param friends ArrayList<String>
     * @return List
     */
    public static List<String> proximityCheck(String regId, ArrayList<String> friends) {
        logger.info("Checking proximity " + regId);
        Key key = KeyFactory.createKey(DEVICE_TYPE, regId);
        Transaction transaction = datastore.beginTransaction();
        List<String> closeFriends = new ArrayList<>();
        try {
            Point userLocation = new Point(datastore.get(key).getProperty(LOCATION).toString());
            for(String item : friends) {
                Point friendLocation = new Point(datastore.get(KeyFactory.stringToKey(item)).getProperty(LOCATION).toString());
                if(new Proximity(userLocation, friendLocation).inCloseProximity()) {
                    closeFriends.add(item);
                }
            }
            transaction.commit();
            return closeFriends;
        } catch(Exception e) {
            logger.severe("Failure in proximityCheck - " + e);
            return Collections.emptyList();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Find friends that are registered on the database.
     *
     * @param friends ArrayList
     * @return List
     */
    public static List<String> findFriends(ArrayList<String> friends) {
        logger.info("Checking registered friends ");
        Transaction transaction = datastore.beginTransaction();
        List<String> closeFriends = new ArrayList<>();
        try {
            for(String item : friends) {
                String friend = datastore.get(KeyFactory.createKey(DEVICE_TYPE, item)).getProperty(LOCATION).toString();
                if(friend != null) {
                    closeFriends.add(item);
                }
            }
            transaction.commit();
            return closeFriends;
        } catch(Exception e) {
            logger.severe("Failure in findFriends - " + e);
            return Collections.emptyList();
        } finally {
            if(transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    /**
     * Find user information from the regId.
     *
     * @param regId String
     * @return Entity
     */
    private static Entity findDeviceByRegId(String regId) {
        Query.Filter propertyFilter = new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL, KeyFactory.createKey(DEVICE_TYPE, regId));
        Query query = new Query(DEVICE_TYPE).setFilter(propertyFilter);
        PreparedQuery preparedQuery = datastore.prepare(query);
        List<Entity> entities = preparedQuery.asList(DEFAULT_FETCH_OPTIONS);
        Entity entity = null;
        if(!entities.isEmpty()) {
            entity = entities.get(0);
        }
        int size = entities.size();
        if(size > 0) {
            logger.info("Found " + size + " entities for regId " + regId + ": " + entities);
        }
        return entity;
    }
}