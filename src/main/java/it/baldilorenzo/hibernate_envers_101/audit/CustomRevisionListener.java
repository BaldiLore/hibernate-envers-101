package it.baldilorenzo.hibernate_envers_101.audit;

import org.hibernate.envers.RevisionListener;

/**
 * Invoked by Envers right before persisting the row in REVINFO.
 * The bean is not managed by Spring, so we retrieve the current user
 * from the ThreadLocal populated by AuditUserFilter.
 */
public class CustomRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity rev = (CustomRevisionEntity) revisionEntity;
        rev.setUsername(AuditUserContext.getUser());
    }
}
