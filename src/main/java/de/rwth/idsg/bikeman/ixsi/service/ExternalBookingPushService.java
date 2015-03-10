package de.rwth.idsg.bikeman.ixsi.service;

import de.rwth.idsg.bikeman.domain.CardAccount;
import de.rwth.idsg.bikeman.domain.Transaction;
import de.rwth.idsg.bikeman.ixsi.IXSIConstants;
import de.rwth.idsg.bikeman.ixsi.api.Producer;
import de.rwth.idsg.bikeman.ixsi.impl.ExternalBookingStore;
import de.rwth.idsg.bikeman.ixsi.schema.BookingTargetIDType;
import de.rwth.idsg.bikeman.ixsi.schema.ExternalBookingPushMessageType;
import de.rwth.idsg.bikeman.ixsi.schema.ExternalBookingType;
import de.rwth.idsg.bikeman.ixsi.schema.IxsiMessageType;
import de.rwth.idsg.bikeman.ixsi.schema.SubscriptionMessageType;
import de.rwth.idsg.bikeman.ixsi.schema.TimePeriodType;
import de.rwth.idsg.bikeman.ixsi.schema.UserInfoType;
import de.rwth.idsg.bikeman.repository.CardAccountRepository;
import de.rwth.idsg.bikeman.repository.MajorCustomerRepository;
import de.rwth.idsg.bikeman.web.rest.dto.view.ViewMajorCustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 24.02.2015
 */
@Slf4j
@Service
public class ExternalBookingPushService {

    @Autowired private Producer producer;
    @Autowired private ExternalBookingStore externalBookingStore;

    @Inject CardAccountRepository cardAccountRepository;
    @Inject MajorCustomerRepository majorCustomerRepository;

    public void report(Long bookingId, Transaction transaction) {
        String cardId = transaction.getCardAccount().getCardId();
        CardAccount cardAccount = cardAccountRepository.findByCardId(cardId);
        ViewMajorCustomerDTO majorCustomer = majorCustomerRepository.findByLogin(cardAccount.getUser().getLogin());


        if (majorCustomer != null) {
            UserInfoType userInfo = new UserInfoType().withUserID(cardId).withProviderID(majorCustomer.getName());
            Set<String> subscribed = externalBookingStore.getSubscribedSystems(userInfo);

            if (subscribed.isEmpty()) {
                log.debug("Will not push. There is no subscribed system for user '{}'", userInfo);
                return;
            }

            // TODO improve timeperiodtype creation: default end time
            DateTime dt = transaction.getStartDateTime().toDateTime();
            TimePeriodType time = new TimePeriodType()
                    .withBegin(dt)
                    .withEnd(dt.plusHours(6));

            BookingTargetIDType bookingTarget = new BookingTargetIDType()
                    .withBookeeID(String.valueOf(transaction.getPedelec().getManufacturerId()))
                    .withProviderID(IXSIConstants.Provider.id);

            ExternalBookingType extBooking = new ExternalBookingType()
                    .withBookingID(String.valueOf(bookingId))
                    .withBookingTargetID(bookingTarget)
                    .withUserInfo(userInfo).withTimePeriod(time);

            ExternalBookingPushMessageType bookingPush = new ExternalBookingPushMessageType().withExternalBooking(extBooking);
            SubscriptionMessageType subscriptionMessageType = new SubscriptionMessageType().withPushMessageGroup(bookingPush);
            IxsiMessageType ixsiMessageType = new IxsiMessageType().withSubscriptionMessage(subscriptionMessageType);

            producer.send(ixsiMessageType, subscribed);
        }


    }

}
