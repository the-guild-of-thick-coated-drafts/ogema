/**
 * Copyright 2011-2018 Fraunhofer-Gesellschaft zur Förderung der angewandten Wissenschaften e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ogema.tools.listener.util.impl;

import org.ogema.core.model.Resource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;

public class StructureEvent implements ResourceStructureEvent {

	private final EventType type;
	private final Resource source;
	private final Resource changed;
	
	public StructureEvent(EventType type, Resource source, Resource changed) {
		this.type = type;
		this.source = source;
		this.changed = changed;
	}
	
	@Override
	public EventType getType() {
		return type;
	}

	@Override
	public Resource getSource() {
		return source;
	}

	@Override
	public Resource getChangedResource() {
		return changed;
	}

}
