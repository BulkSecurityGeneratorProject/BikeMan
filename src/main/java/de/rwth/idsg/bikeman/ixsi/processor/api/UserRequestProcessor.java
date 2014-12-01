package de.rwth.idsg.bikeman.ixsi.processor.api;

import com.google.common.base.Optional;
import de.rwth.idsg.bikeman.ixsi.schema.ErrorType;
import de.rwth.idsg.bikeman.ixsi.schema.Language;
import de.rwth.idsg.bikeman.ixsi.schema.UserInfoType;
import de.rwth.idsg.ixsi.jaxb.UserTriggeredRequestChoice;
import de.rwth.idsg.ixsi.jaxb.UserTriggeredResponseChoice;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.10.2014
 */
public interface UserRequestProcessor<T1 extends UserTriggeredRequestChoice, T2 extends UserTriggeredResponseChoice> {

    T2 processAnonymously(T1 request, Optional<Language> lan);

    /**
     * This method has to validate the user infos !!!!
     */
    T2 processForUser(T1 request, Optional<Language> lan,
                      List<UserInfoType> userInfoList);

    T2 buildError(ErrorType e);
}