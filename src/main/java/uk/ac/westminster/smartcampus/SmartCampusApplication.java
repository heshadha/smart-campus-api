package uk.ac.westminster.smartcampus;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import uk.ac.westminster.smartcampus.exception.GlobalExceptionMapper;
import uk.ac.westminster.smartcampus.exception.LinkedResourceNotFoundMapper;
import uk.ac.westminster.smartcampus.exception.RoomNotEmptyMapper;
import uk.ac.westminster.smartcampus.exception.SensorUnavailableMapper;
import uk.ac.westminster.smartcampus.filter.CampusLoggingFilter;
import uk.ac.westminster.smartcampus.resource.DiscoveryResource;
import uk.ac.westminster.smartcampus.resource.SensorResource;
import uk.ac.westminster.smartcampus.resource.SensorRoom;

// establishes the versioned routing gateway for all campus infrastructure requests
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        
        // registers primary endpoints for room and sensor discovery
        resources.add(DiscoveryResource.class);
        resources.add(SensorRoom.class);
        resources.add(SensorResource.class);
        
        // registers exception mappers to ensure leak-proof error handling
        resources.add(RoomNotEmptyMapper.class);
        resources.add(LinkedResourceNotFoundMapper.class);
        resources.add(SensorUnavailableMapper.class);
        resources.add(GlobalExceptionMapper.class);
        
        // registers the global filter for request and response logging
        resources.add(CampusLoggingFilter.class);
        
        return resources;
    }
}