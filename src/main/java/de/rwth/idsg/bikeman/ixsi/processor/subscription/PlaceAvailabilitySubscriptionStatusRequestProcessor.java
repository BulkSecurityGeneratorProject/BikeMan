package de.rwth.idsg.bikeman.ixsi.processor.subscription;

import de.rwth.idsg.bikeman.ixsi.ErrorFactory;
import de.rwth.idsg.bikeman.ixsi.schema.PlaceAvailabilitySubscriptionStatusRequest;
import de.rwth.idsg.bikeman.ixsi.schema.PlaceAvailabilitySubscriptionStatusResponse;
import org.springframework.stereotype.Component;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Component
public class PlaceAvailabilitySubscriptionStatusRequestProcessor implements
        SubscriptionRequestProcessor<PlaceAvailabilitySubscriptionStatusRequest, PlaceAvailabilitySubscriptionStatusResponse> {

    @Override
    public PlaceAvailabilitySubscriptionStatusResponse process(PlaceAvailabilitySubscriptionStatusRequest request) {
        return null;
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Override
    public PlaceAvailabilitySubscriptionStatusResponse invalidSystem() {
        PlaceAvailabilitySubscriptionStatusResponse b = new PlaceAvailabilitySubscriptionStatusResponse();
        b.getError().add(ErrorFactory.invalidSystem());
        return b;
    }
}