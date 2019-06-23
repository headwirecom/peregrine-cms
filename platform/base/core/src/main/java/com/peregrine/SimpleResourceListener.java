//package com.peregrine.nodetypes;
//
//import javax.jcr.RepositoryException;
//import javax.jcr.Session;
//import javax.jcr.observation.Event;
//import javax.jcr.observation.EventIterator;
//import javax.jcr.observation.EventListener;
//import org.apache.sling.jcr.api.SlingRepository;
//import org.osgi.service.component.ComponentContext;
//import org.osgi.service.component.annotations.Activate;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.Deactivate;
//import org.osgi.service.component.annotations.Reference;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@Component(immediate = true, service = EventListener.class)
//public class SimpleResourceListener implements EventListener {
//
//  private static final Logger log = LoggerFactory.getLogger(SimpleResourceListener.class);
//
//  @Reference
//  SlingRepository repository;
//
//  Session session = null;
//
//  @Activate
//  public void activate(ComponentContext context) throws Exception {
//    log.info("activating ExampleObservation");
//    try {
//      session = repository.loginAdministrative(null);
//      session.getWorkspace().getObservationManager().addEventListener(
//          this, //handler
//          Event.NODE_ADDED | Event.NODE_REMOVED | Event.NODE_MOVED | Event.PROPERTY_ADDED
//              | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED,
//          //binary combination of event types
//          "/content/sites", //path
//          true, //is Deep?
//          null, //uuids filter
//          null, //nodetypes filter
//          false);
//    } catch (RepositoryException e) {
//      log.error("unable to register session", e);
//      throw new Exception(e);
//    }
//  }
//
//  @Deactivate
//  public void deactivate() {
//    if (session != null) {
//      session.logout();
//    }
//  }
//
//  public void onEvent(EventIterator eventIterator) {
//    try {
//      while (eventIterator.hasNext()) {
//        Event event = eventIterator.nextEvent();
//        switch (event.getType()) {
//          case Event.NODE_ADDED:
//            log.error("Node created at: " + event.getPath());
//          case Event.NODE_MOVED:
//            log.error("Node change to: " + event.getPath());
//          case Event.NODE_REMOVED:
//            log.error("Node Removed from: " + event.getPath());
//            break;
//          case Event.PROPERTY_ADDED:
//          case Event.PROPERTY_REMOVED:
//          case Event.PROPERTY_CHANGED:
//            log.error("Property: " + event.getInfo());
//            break;
//          default:
//            break;
//
//        }
//
////        if (eventIterator.nextEvent().getType()) {
////
////        } else if (eventIterator.nextEvent().getType()) {
////          log.info("something has been added : {}", eventIterator.nextEvent().getPath());
////        }
////
//
//      }
//    } catch (RepositoryException e) {
//      log.error("Error while treating events", e);
//    }
//  }
//}