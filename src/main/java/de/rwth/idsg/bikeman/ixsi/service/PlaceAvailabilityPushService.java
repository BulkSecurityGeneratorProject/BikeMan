package de.rwth.idsg.bikeman.ixsi.service;

import de.rwth.idsg.bikeman.ixsi.IXSIConstants;
import de.rwth.idsg.bikeman.ixsi.api.Producer;
import de.rwth.idsg.bikeman.ixsi.processor.PlaceAvailabilityStore;
import de.rwth.idsg.bikeman.ixsi.repository.QueryIXSIRepository;
import de.rwth.idsg.bikeman.ixsi.schema.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 10.11.2014
 */
@Slf4j
@Service
public class PlaceAvailabilityPushService {

    @Autowired private Producer producer;
    @Autowired private PlaceAvailabilityStore placeAvailabilityStore;
    @Autowired private QueryIXSIRepository queryIXSIRepository;

    public void reportChange(String placeID) {

        Integer freeSlots = queryIXSIRepository.placeAvailability(Arrays.asList(placeID))
                                               .get(0)
                                               .getAvailableSlots();

        ProviderPlaceIDType placeIDType = new ProviderPlaceIDType();
        placeIDType.setPlaceID(placeID);
        placeIDType.setProviderID(IXSIConstants.Provider.id);

        PlaceAvailabilityType avail = new PlaceAvailabilityType();
        avail.setAvailability(freeSlots);
        avail.setID(placeIDType);

        PlaceAvailabilityPushMessageType push = new PlaceAvailabilityPushMessageType();
        push.getPlaceAvailability().add(avail);

        SubscriptionMessageType sub = new SubscriptionMessageType();
        sub.setPushMessageGroup(push);

        IxsiMessageType ixsi = new IxsiMessageType();
        ixsi.setSubscriptionMessage(sub);

        Set<String> systemIdSet = placeAvailabilityStore.getSubscribedSystems(placeID);
        producer.send(ixsi, systemIdSet);
    }

}
