package com.mediconnect.security;

import com.mediconnect.model.Role;
import com.mediconnect.service.SessionManager;

import jakarta.faces.application.NavigationHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

import java.io.Serializable;

/**
 * PhaseListener that enforces role-based access control (RBAC) on URL paths.
 *
 * Protected paths:
 * /views/medecin/* → only MEDECIN role
 * /views/secretaire/* → only SECRETAIRE role
 * /views/patient/* → only PATIENT role
 *
 * Public paths (no auth required):
 * /login.xhtml
 * /access-denied.xhtml
 * /javax.faces.resource/* (JSF resources - CSS, JS, images)
 */
public class AuthorizationPhaseListener implements PhaseListener, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        String viewId = context.getViewRoot().getViewId();

        // Allow public resources
        if (isPublicResource(viewId)) {
            return;
        }

        // Get SessionManager from CDI
        SessionManager sessionManager = getSessionManager(context);

        // If not logged in, redirect to login
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            redirectTo(context, "/login.xhtml");
            return;
        }

        // Check role-based access
        if (!isAuthorized(viewId, sessionManager)) {
            redirectTo(context, "/access-denied.xhtml");
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        // No action needed before the phase
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    /**
     * Check if the requested view is a public resource (no auth required).
     */
    private boolean isPublicResource(String viewId) {
        if (viewId == null)
            return true;
        return viewId.contains("/login.xhtml")
                || viewId.contains("/signup.xhtml")
                || viewId.contains("/access-denied.xhtml")
                || viewId.startsWith("/jakarta.faces.resource")
                || viewId.startsWith("/javax.faces.resource");
    }

    /**
     * Check if the current user's role is authorized to access the requested view.
     */
    private boolean isAuthorized(String viewId, SessionManager sessionManager) {
        if (viewId == null)
            return true;

        Role role = sessionManager.getCurrentRole();
        if (role == null)
            return false;

        // Check protected paths
        if (viewId.startsWith("/views/medecin/")) {
            return role == Role.MEDECIN;
        }
        if (viewId.startsWith("/views/secretaire/")) {
            return role == Role.SECRETAIRE;
        }
        if (viewId.startsWith("/views/patient/")) {
            return role == Role.PATIENT;
        }

        // Any other view: allow if logged in
        return true;
    }

    /**
     * Redirect to a target page.
     */
    private void redirectTo(FacesContext context, String targetViewId) {
        NavigationHandler navHandler = context.getApplication().getNavigationHandler();
        navHandler.handleNavigation(context, null, targetViewId + "?faces-redirect=true");
        context.renderResponse();
    }

    /**
     * Get the SessionManager bean from the CDI/EL context.
     */
    private SessionManager getSessionManager(FacesContext context) {
        try {
            return (SessionManager) context.getApplication()
                    .evaluateExpressionGet(context, "#{sessionManager}", SessionManager.class);
        } catch (Exception e) {
            return null;
        }
    }
}
