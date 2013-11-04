/**
 * Copyright (C) 2010-2013 Axel Morgner, structr <structr@structr.org>
 *
 * This file is part of structr <http://structr.org>.
 *
 * structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.structr.web.entity.relation;

import org.structr.core.property.Property;
import org.structr.common.PropertyView;
import org.structr.common.View;
import org.structr.core.entity.ManyToMany;
import org.structr.core.property.StringProperty;
import org.structr.core.graph.NodeInterface;
import org.structr.web.entity.Linkable;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author Christian Morgner
 */
public class AbstractLink extends ManyToMany<NodeInterface, Linkable> {

	public static final Property<String> type     = new StringProperty("type");

	public static final View uiView = new View(AbstractLink.class, PropertyView.Ui,
		type	
	);

	@Override
	public Class<Linkable> getTargetType() {
		return Linkable.class;
	}

	@Override
	public String name() {
		return "LINK";
	}

	@Override
	public Class<NodeInterface> getSourceType() {
		return NodeInterface.class;
	}
}