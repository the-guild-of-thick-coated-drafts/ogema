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
package org.ogema.resourcemanager.impl.model.units;

import org.ogema.core.model.Resource;
import org.ogema.core.model.units.BrightnessResource;
import org.ogema.core.model.units.PhysicalUnit;
import org.ogema.resourcemanager.impl.ApplicationResourceManager;

import org.ogema.resourcemanager.virtual.VirtualTreeElement;

/**
 *
 * @author Timo Fischer, Fraunhofer IWES
 */
public class DefaultBrightnessResource extends UnitFloatResource implements BrightnessResource {

	public DefaultBrightnessResource(VirtualTreeElement el, Class<? extends Resource> unitResourceType, String path,
			ApplicationResourceManager resMan) {
		super(el, unitResourceType, path, resMan);
	}

	@Override
	public final PhysicalUnit getUnit() {
		return PhysicalUnit.LUX;
	}
}
