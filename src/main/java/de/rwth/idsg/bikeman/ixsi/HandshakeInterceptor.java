package de.rwth.idsg.bikeman.ixsi;

import com.google.common.base.Strings;
import de.rwth.idsg.bikeman.config.IxsiConfiguration;
import de.rwth.idsg.bikeman.ixsi.repository.SystemValidator;
import de.rwth.idsg.bikeman.web.rest.exception.DatabaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by max on 08/09/14.
 */
@Slf4j
@RequiredArgsConstructor
public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    private final SystemValidator systemValidator;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String systemId = getSystemID(request);
        if (systemId == null) {
            throw new DatabaseException("This ip address is not allowed for WebSocket communication");
        } else {
            // to be be used in future
            attributes.put(IxsiConfiguration.SYSTEM_ID_KEY, systemId);
            return super.beforeHandshake(request, response, wsHandler, attributes);
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception ex) {
        super.afterHandshake(request, response, wsHandler, ex);
        log.trace("Handshake complete with {}", request.getRemoteAddress());
    }

    private String getSystemID(ServerHttpRequest request) {
        Set<String> ipAddresses = getPossibleIpAddresses(request);
        log.info("ipAddresses for this request: {}", ipAddresses);

        for (String ip : ipAddresses) {
            try {
                return systemValidator.getSystemID(ip);
            } catch (DatabaseException e) {
                // not in db, continue with the next in the list
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Since Spring uses many abstractions for different APIs, we try every
    // possible extraction method there available.
    // -------------------------------------------------------------------------

    private static Set<String> getPossibleIpAddresses(ServerHttpRequest request) {
        Set<String> ipAddressList = new HashSet<>();

        getFromProxy(request).stream()
                             .filter(s -> !Strings.isNullOrEmpty(s))
                             .forEach(ipAddressList::add);

        ipAddressList.add(getWithInstanceOf(request));
        ipAddressList.add(getFromRemote(request));
        ipAddressList.add(getFromContext());

        return ipAddressList;
    }

    private static Set<String> getFromProxy(ServerHttpRequest request) {
        List<String> strings = request.getHeaders().get("X-FORWARDED-FOR");
        if (strings == null) {
            return Collections.emptySet();
        } else {
            return new HashSet<>(strings);
        }
    }

    private static String getWithInstanceOf(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String ipAddress = servletRequest.getServletRequest().getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = servletRequest.getServletRequest().getRemoteAddr();
            }
            return ipAddress;
        }
        return null;
    }

    private static String getFromRemote(ServerHttpRequest request) {
        InetSocketAddress inetSocketAddress = request.getRemoteAddress();
        return inetSocketAddress.getAddress().getHostAddress();
    }

    private static String getFromContext() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getRemoteAddr();
    }
}
