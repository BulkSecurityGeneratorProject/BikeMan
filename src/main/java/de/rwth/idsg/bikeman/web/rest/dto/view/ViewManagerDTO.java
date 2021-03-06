package de.rwth.idsg.bikeman.web.rest.dto.view;

import lombok.Data;

/**
 * Created by swam on 11/06/14.
 */
@Data
public class ViewManagerDTO {
    private final Long userId;
    private final String login;
    private final String cardId;
    private final String cardPin;
}