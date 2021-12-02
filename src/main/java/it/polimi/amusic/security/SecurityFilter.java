package it.polimi.amusic.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import it.polimi.amusic.exception.FirebaseException;
import it.polimi.amusic.model.document.UserDocument;
import it.polimi.amusic.security.model.Credentials;
import it.polimi.amusic.service.business.UserBusinessService;
import it.polimi.amusic.service.persistance.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final SecurityService securityService;
    private final UserService userService;
    private final UserBusinessService userBusinessService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        verifyToken(request);
        filterChain.doFilter(request, response);
    }

    private void verifyToken(HttpServletRequest request) {
        FirebaseToken decodedToken = null;
        Credentials.CredentialType type = null;
        String token = securityService.getBearerToken(request);
        log.info("token {}",token);
        try {
                if (token != null && !token.equalsIgnoreCase("undefined")) {
                    //Controllo se il token di Firebase é valido
                    //Nel caso in cui non fosse valido il metodo verifyIdToken lancerebbe un exception
                    decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                    type = Credentials.CredentialType.ID_TOKEN;
                }
        } catch (FirebaseAuthException e) {
            log.error("Firebase Exception {} ", e.getLocalizedMessage());
            throw new FirebaseException("Firebase exception");
        }

        UserDocument user = firebaseTokenToUserDto(decodedToken);

        if (user != null && user.isEnabled()) {
            //Creo un AuthenticationToken , come principal é l user e come Credentials sara il token di firebase
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
                    new Credentials(type, decodedToken, token), user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            //Imposto nel security context l'autenticazione
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    /**
     * Tramite il token recupero l email e l user associato
     *
     * @param decodedToken
     * @return UserDocument
     */
    private UserDocument firebaseTokenToUserDto(FirebaseToken decodedToken) {
        UserDocument user = null;
        if (decodedToken != null) {
            user = userService.findByEmail(decodedToken.getEmail()).
                    map(userDocument -> userService.updateFromFirebase(userDocument, decodedToken))
                    .orElseGet(() -> userBusinessService.registerUser(decodedToken));
        }
        return user;
    }

}

