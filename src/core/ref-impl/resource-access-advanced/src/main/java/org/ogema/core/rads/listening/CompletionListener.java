/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ogema.core.rads.listening;

import java.util.ArrayList;
import java.util.List;

import org.ogema.core.application.ApplicationManager;
import org.ogema.core.logging.OgemaLogger;
import org.ogema.core.model.Resource;
import org.ogema.core.rads.tools.RadFactory;
import org.ogema.core.rads.tools.ResourceFieldInfo;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.core.resourcemanager.pattern.PatternListener;
import org.ogema.core.resourcemanager.pattern.ResourcePattern;
import org.ogema.core.resourcemanager.pattern.ResourcePattern.CreateMode;

/**
 * Takes a RAD with a matched primary demand and checks if all required fields are set.
 */
class CompletionListener<P extends ResourcePattern<?>>  {

	public static final boolean debug = true;
	public static final boolean tryFixLists = false;

	private final ApplicationManager m_appMan;
	private final OgemaLogger m_logger;
	private final P m_rad;
	// List of required fields
	private final List<ConnectedResource> m_required = new ArrayList<>();
	// List of optional fields
	private final List<ConnectedResource> m_optional = new ArrayList<>();
	// List of fields that are to be created
	// private final List<ConnectedResource> m_create = new ArrayList<>();

	PatternListener<P> m_listener;

	// a patternAvailable callback is issued as soon as both m_completed and init_satisfied are true;
	// as soon as one of them turns false, patternUnavailable is called. 
	// !m_completed => !init_satisfied
	private boolean m_completed = false;	
	private boolean init_satisfied = false;

	/*
	 * Gets all the fields annotated in the RAD, irrespective of their existence requirements.
	 */
	private List<ConnectedResource> getAllConnectedResources() {
		final List<ConnectedResource> result = new ArrayList<>(m_required.size() + m_optional.size());
		result.addAll(m_required);
		result.addAll(m_optional);
		// result.addAll(m_create);
		return result;
	}

	public CompletionListener(ApplicationManager appMan, P rad, final List<ResourceFieldInfo> fields) {
		m_appMan = appMan;
		m_logger = appMan.getLogger();
		m_rad = rad;
		for (ResourceFieldInfo info : fields) {
			final Resource resource = RadFactory.getResource(info.getField(), rad);
			final CreateMode mode = info.getCreateMode();
			if (resource == null) {
				continue;   // ignore uninitialized resources
			}
			if (mode == CreateMode.MUST_EXIST) {
				m_required.add(new ConnectedResource(resource, info, this));
			}
			else if (mode == CreateMode.OPTIONAL) {
				m_optional.add(new ConnectedResource(resource, info, this));
			}
			else {
				throw new RuntimeException("Unsupported create mode " + mode);
			}
		}
	}

	public void start(PatternListener<P> completionListener) {
		m_listener = completionListener;
		for (ConnectedResource conRes : getAllConnectedResources()) {
			conRes.start();
		}
	}

	public void stop() {
		for (ConnectedResource conRes : getAllConnectedResources()) {
			conRes.stop();
		}
		m_completed = false;
		init_satisfied = false;
	}

	private void checkCompletion() {
		for (ConnectedResource conRes : m_required) {
			if (!conRes.isComplete()) {
				m_completed = false;
				init_satisfied = false;
				return;
			}
		}

		m_completed = true;
		m_logger.debug("Completed a RAD of type " + m_rad.getClass().getCanonicalName() + " with primary demand "
				+ ((Resource) m_rad.model).getLocation());
		checkInitRequirement();
		for (ConnectedResource cr: getAllConnectedResources()) {
			if (cr.requiresValueListener()) {
				cr.startValueListener();
			}
		}
		
	}

	public void resourceAvailable(ConnectedResource conRes) {
		//if (conRes.isRequired() && !m_completed)
		checkCompletion();
	}

	public void resourceUnavailable(ConnectedResource conRes, boolean isDeleted) {		
		//System.out.println("  res unavailable callback " + conRes.toString());
		if (!conRes.isRequired()) {
			if (m_completed) checkInitRequirement();
			return;
		}
		if (m_completed && init_satisfied) {
			m_listener.patternUnavailable(m_rad);
		}
		m_completed = false;	
		init_satisfied = false;
		for (ConnectedResource cr: getAllConnectedResources()) {
			cr.stopValueListener();
		}
		
	}
	
	public boolean isComplete() {
		return m_completed;
	}
	
	public boolean isSatisfied() {
		return init_satisfied;
	}

	public void valueChanged(ConnectedResource cr) {
		if (!m_completed) {
			throw new RuntimeException("Received a value changed callback, although the pattern structure is not complete");
		}
		checkInitRequirement();
	}
	
	private void checkInitRequirement() {
		if (m_rad.accept()) {
			if (init_satisfied) return;
			init_satisfied = true;
			m_listener.patternAvailable(m_rad);
		}
		else {
			if (!init_satisfied) return;
			init_satisfied = false;
			m_listener.patternUnavailable(m_rad);
		}
	}
}
