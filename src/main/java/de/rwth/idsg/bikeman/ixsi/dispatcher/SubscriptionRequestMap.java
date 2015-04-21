package de.rwth.idsg.bikeman.ixsi.dispatcher;

import de.rwth.idsg.bikeman.ixsi.IxsiProcessingException;
import de.rwth.idsg.bikeman.ixsi.processor.api.SubscriptionRequestProcessor;
import de.rwth.idsg.bikeman.ixsi.processor.subscription.request.*;
import de.rwth.idsg.bikeman.ixsi.schema.*;
import de.rwth.idsg.ixsi.jaxb.SubscriptionRequestGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 21.10.2014
 */
@Slf4j
@Component
public class SubscriptionRequestMap extends HashMap<Class<?>, SubscriptionRequestProcessor> {
    private static final long serialVersionUID = -8483982822596525355L;

    @Autowired private AvailabilitySubscriptionRequestProcessor availabilitySubscriptionRequestProcessor;
    @Autowired private AvailabilitySubscriptionStatusRequestProcessor availabilitySubscriptionStatusRequestProcessor;
    @Autowired private PlaceAvailabilitySubscriptionRequestProcessor placeAvailabilitySubscriptionRequestProcessor;
    @Autowired private PlaceAvailabilitySubscriptionStatusRequestProcessor placeAvailabilitySubscriptionStatusRequestProcessor;
    @Autowired private BookingAlertSubscriptionRequestProcessor bookingAlertSubscriptionRequestProcessor;
    @Autowired private BookingAlertSubscriptionStatusRequestProcessor bookingAlertSubscriptionStatusRequestProcessor;
    @Autowired private ConsumptionSubscriptionRequestProcessor consumptionSubscriptionRequestProcessor;
    @Autowired private ConsumptionSubscriptionStatusRequestProcessor consumptionSubscriptionStatusRequestProcessor;
    @Autowired private ExternalBookingSubscriptionRequestProcessor externalBookingSubscriptionRequestProcessor;
    @Autowired private ExternalBookingSubscriptionStatusRequestProcessor externalBookingSubscriptionStatusRequestProcessor;

    @PostConstruct
    public void init() {
        super.put(AvailabilitySubscriptionRequestType.class, availabilitySubscriptionRequestProcessor);
        super.put(AvailabilitySubscriptionStatusRequestType.class, availabilitySubscriptionStatusRequestProcessor);
        super.put(PlaceAvailabilitySubscriptionRequestType.class, placeAvailabilitySubscriptionRequestProcessor);
        super.put(PlaceAvailabilitySubscriptionStatusRequestType.class, placeAvailabilitySubscriptionStatusRequestProcessor);
        super.put(BookingAlertSubscriptionRequestType.class, bookingAlertSubscriptionRequestProcessor);
        super.put(BookingAlertSubscriptionStatusRequestType.class, bookingAlertSubscriptionStatusRequestProcessor);
        super.put(ConsumptionSubscriptionRequestType.class, consumptionSubscriptionRequestProcessor);
        super.put(ConsumptionSubscriptionStatusRequestType.class, consumptionSubscriptionStatusRequestProcessor);
        super.put(ExternalBookingSubscriptionRequestType.class, externalBookingSubscriptionRequestProcessor);
        super.put(ExternalBookingSubscriptionStatusRequestType.class, externalBookingSubscriptionStatusRequestProcessor);
        log.trace("Ready");
    }

    public SubscriptionRequestProcessor find(SubscriptionRequestGroup s) {
        Class<?> clazz = s.getClass();
        SubscriptionRequestProcessor p = super.get(clazz);
        if (p == null) {
            throw new IxsiProcessingException("No processor is registered for the incoming request of type: " + clazz);
        } else {
            return p;
        }
    }
}