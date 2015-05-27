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
package org.ogema.resourcemanager.impl;

import java.util.concurrent.Callable;
import org.ogema.core.administration.AdminApplication;
import org.ogema.core.administration.RegisteredValueListener;
import org.ogema.core.application.ApplicationManager;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceValueListener;

/**
 * Represents a listener registration generated by a call to
 * {@link Resource#addValueListener(org.ogema.core.resourcemanager.ResourceValueListener) }.
 * 
 * @author jlapp
 */
public class ValueListenerRegistration implements ResourceListenerRegistration, RegisteredValueListener {

	protected final ResourceBase origin;
	@SuppressWarnings("rawtypes")
	protected final ResourceValueListener listener;
	protected final boolean callOnEveryUpdate;

	public ValueListenerRegistration(ResourceBase origin, ResourceValueListener<?> listener, boolean callOnEveryUpdate) {
		this.origin = origin;
		this.listener = listener;
		this.callOnEveryUpdate = callOnEveryUpdate;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ValueListenerRegistration)) {
			return false;
		}
		ValueListenerRegistration other = (ValueListenerRegistration) obj;
		return other.listener == listener && other.origin.equals(origin);
	}

	@Override
	public int hashCode() {
		return origin.hashCode();
	}

	@Override
	public void queueResourceChangedEvent(final Resource r, boolean valueChanged) {
		if (!callOnEveryUpdate && !valueChanged) {
			return;
		}
		Callable<Void> listenerCall = new Callable<Void>() {

			@Override
			@SuppressWarnings("unchecked")
			public Void call() throws Exception {
				listener.resourceChanged(r);
				return null;
			}
		};
		origin.resMan.getApplicationManager().submitEvent(listenerCall);
	}

	@Override
	public void performRegistration() {
		final ResourceDBManager manager = origin.resMan.getDatabaseManager();
		ElementInfo info = manager.getElementInfo(origin.getEl());
		info.addResourceListener(this);
	}

	@Override
	public void unregister() {
		final ResourceDBManager manager = origin.resMan.getDatabaseManager();
		ElementInfo info = manager.getElementInfo(origin.getEl());
		info.removeResourceListener(this);
	}

	@Override
	public Resource getResource() {
		return origin;
	}

	@Override
	public AdminApplication getApplication() {
		ApplicationManager appman = origin.resMan.getApplicationManager();
		return appman.getAdministrationManager().getAppById(appman.getAppID().getIDString());
	}

	@Override
	public boolean isCallOnEveryUpdate() {
		return callOnEveryUpdate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> ResourceValueListener<T> getValueListener() {
		return listener;
	}

	@Override
	public boolean isAbandoned() {
		return false;
	}

	@Override
	public boolean isRecursive() {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public org.ogema.core.resourcemanager.ResourceListener getListener() {
		return null;
	}

}
