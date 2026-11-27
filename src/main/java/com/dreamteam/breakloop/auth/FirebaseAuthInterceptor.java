package com.dreamteam.breakloop.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Requires {@code Authorization: Bearer <Firebase ID token>}: 401 if missing
 * or invalid. By default the token's uid must also match the {uid} of the route
 * (403 otherwise); {@link #authenticationOnly} skips that check and leaves the
 * access rule to the service. Only registered for the new endpoints (see
 * WebConfig).
 */
public class FirebaseAuthInterceptor implements HandlerInterceptor {

    private static final String BEARER = "Bearer ";

    /** Request attribute with the token's email (may be null). */
    public static final String TOKEN_EMAIL_ATTRIBUTE =
            FirebaseAuthInterceptor.class.getName() + ".email";

    /** Request attribute with the token's uid. */
    public static final String TOKEN_UID_ATTRIBUTE =
            "com.dreamteam.breakloop.auth.FirebaseAuthInterceptor.uid";

    // Resolved per request so a context without Firebase (such as a
    // @WebMvcTest) still starts.
    private final Supplier<FirebaseAuth> firebaseAuth;

    private final boolean requireOwner;

    public FirebaseAuthInterceptor(Supplier<FirebaseAuth> firebaseAuth) {
        this(firebaseAuth, true);
    }

    private FirebaseAuthInterceptor(
            Supplier<FirebaseAuth> firebaseAuth, boolean requireOwner) {
        this.firebaseAuth = firebaseAuth;
        this.requireOwner = requireOwner;
    }

    /** Validates the token without comparing it to the route. */
    public static FirebaseAuthInterceptor authenticationOnly(
            Supplier<FirebaseAuth> firebaseAuth) {
        return new FirebaseAuthInterceptor(firebaseAuth, false);
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        // CORS preflight carries no credentials
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith(BEARER)) {
            return reject(response, 401, "Missing bearer token");
        }

        FirebaseToken token;

        try {
            token = firebaseAuth.get().verifyIdToken(
                    header.substring(BEARER.length()).trim());
        } catch (FirebaseAuthException | IllegalArgumentException e) {
            return reject(response, 401, "Invalid token");
        }

        request.setAttribute(TOKEN_UID_ATTRIBUTE, token.getUid());
        request.setAttribute(TOKEN_EMAIL_ATTRIBUTE, token.getEmail());

        if (!requireOwner) {
            return true;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> variables = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String uid = variables == null ? null : variables.get("uid");

        if (uid == null || !uid.equals(token.getUid())) {
            return reject(response, 403, "Token does not belong to this user");
        }

        return true;
    }

    private boolean reject(
            HttpServletResponse response, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"error\":\"" + (status == 401 ? "Unauthorized" : "Forbidden")
                        + "\",\"message\":\"" + message + "\"}");

        return false;
    }
}
