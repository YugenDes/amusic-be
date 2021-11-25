package it.polimi.amusic.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final HttpServletRequest httpServletRequest;
    private final SecurityProperties securityProps;

    public boolean isPublic() {
        return securityProps.getAllowedPublicApis().contains(httpServletRequest.getRequestURI());
    }

    /**
     * Estrae il token bearer dall header Authorization
     *
     * @param request
     * @return token senza 'Bearer '
     */
    public String getBearerToken(HttpServletRequest request) {
        String bearerToken = null;
        //Recuper il token dall header Authorization della request
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            //Effettuo il substring per poter prendere il token da far validare a firebase
            bearerToken = authorization.substring(7);
        }
        return bearerToken;
    }

}
