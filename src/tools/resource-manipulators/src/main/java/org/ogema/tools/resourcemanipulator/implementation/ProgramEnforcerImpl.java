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
package org.ogema.tools.resourcemanipulator.implementation;

import org.ogema.tools.resourcemanipulator.ResourceManipulatorImpl;
import org.ogema.core.model.Resource;
import org.ogema.core.model.ValueResource;
import org.ogema.core.resourcemanager.AccessPriority;
import org.ogema.tools.resourcemanipulator.configurations.ProgramEnforcer;
import org.ogema.tools.resourcemanipulator.model.ProgramEnforcerModel;

/**
 * @author Timo Fischer, Fraunhofer IWES
 */
public class ProgramEnforcerImpl implements ProgramEnforcer {

	private final ResourceManipulatorImpl m_base;
	private Resource m_targetResource;
	private long m_updateInterval;
	private AccessPriority m_priority;
	private boolean m_exclusiveAccessRequired;

	// Configuration this is connected to (null if not connected)
	private ProgramEnforcerModel m_config;

	/**
	 * Creates an instance of the configuration object from an existing
	 * configuration.
	 */
	public ProgramEnforcerImpl(ResourceManipulatorImpl base, ProgramEnforcerModel configResource) {
		m_base = base;
		m_targetResource = configResource.targetResource();
		m_updateInterval = configResource.updateInterval().getValue();
		m_priority = AccessPriority.valueOf(configResource.priority().getValue());
		m_exclusiveAccessRequired = configResource.exclusiveAccessRequired().getValue();
		m_config = configResource;
	}

	public ProgramEnforcerImpl(ResourceManipulatorImpl base) {
		m_base = base;
		m_targetResource = null;
		m_updateInterval = 10000l;
		m_priority = AccessPriority.PRIO_LOWEST;
		m_exclusiveAccessRequired = false;
		m_config = null;
	}

	@Override
	public boolean commit() {
		if (m_targetResource == null) {
			return false;
		}
		// delete the old configuration if it exsited.
		if (m_config != null)
			m_config.delete();

		m_config = m_base.createResource(ProgramEnforcerModel.class);

		m_config.targetResource().setAsReference(m_targetResource);
		m_config.updateInterval().create();
		m_config.updateInterval().setValue(m_updateInterval);
		m_config.exclusiveAccessRequired().create();
		m_config.exclusiveAccessRequired().setValue(m_exclusiveAccessRequired);
		m_config.priority().create();
		m_config.priority().setValue(m_priority.toString());

		m_config.activate(true);
		return true;
	}

	@Override
	public void remove() {
		if (m_config != null) {
			m_config.delete();
		}
	}

	@Override
	public void enforceProgram(ValueResource resource, long updateInterval, AccessPriority priority) {
		m_targetResource = resource;
		m_updateInterval = updateInterval;
		if (priority == null) {
			m_priority = AccessPriority.PRIO_LOWEST;
			m_exclusiveAccessRequired = false;
		}
		else {
			m_priority = priority;
			m_exclusiveAccessRequired = true;
		}
	}

	@Override
	public void enforceProgram(ValueResource resource, long updateInterval) {
		m_targetResource = resource;
		m_updateInterval = updateInterval;
		m_priority = AccessPriority.PRIO_LOWEST;
		m_exclusiveAccessRequired = false;
	}

	@Override
	public AccessPriority getAccessPriority() {
		return (m_exclusiveAccessRequired) ? m_priority : null;
	}

	@Override
	public long getUpdateInterval() {
		return m_updateInterval;
	}

}
