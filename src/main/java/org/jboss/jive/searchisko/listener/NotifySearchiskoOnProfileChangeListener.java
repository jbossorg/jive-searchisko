/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 */
package org.jboss.jive.searchisko.listener;

import java.util.Map;
import java.util.Set;

import com.jivesoftware.base.User;
import com.jivesoftware.base.UserManager;
import com.jivesoftware.base.UserNotFoundException;
import com.jivesoftware.base.event.v2.InlineEventListener;
import com.jivesoftware.community.event.ProfileEvent;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.jive.searchisko.SearchiskoManager;
import org.jboss.jive.searchisko.SearchiskoManagerImpl;
import org.jboss.jive.searchisko.SearchiskoUpdateAccountProfileExecutor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Listener on ProfileEvent
 *
 * @author Libor Krzyzanek
 */
public class NotifySearchiskoOnProfileChangeListener implements InlineEventListener<ProfileEvent> {

	private static final Logger log = LogManager.getLogger(NotifySearchiskoOnProfileChangeListener.class);

	@Autowired
	private SearchiskoManager searchiskoManager;

	@Autowired
	private UserManager userManager;

	@SuppressWarnings("unchecked")
	@Override
	public void handle(ProfileEvent e) {
		if (!SearchiskoUpdateAccountProfileExecutor.isEnabled()) {
			log.debug("SearchiskoUpdateAccountProfile is disabled");
			return;
		}

		if (ProfileEvent.Type.MODIFIED.compareTo(e.getType()) == 0) {
			if (log.isTraceEnabled()) {
				log.trace("Profile change event: " + e);
				log.trace("params: " + e.getParams());
				log.trace("user: " + e.getPayload());
			}

			long userId = e.getPayload().getID();
			String username;
			try {
				username = userManager.getUser(userId).getUsername();
			} catch (UserNotFoundException e1) {
				log.error("Cannot find username with id: " + userId);
				return;
			}
			// Notify searchisko on ANY profile change
			// Map<String, ?> params = e.getParams();
			// Set<Long> modifiedFieldIds = (Set<Long>) params.get(ProfileEvent.PARAM_MODIFIED_FIELD_IDS);

			searchiskoManager.accountChanged(username);
		}
	}

}
