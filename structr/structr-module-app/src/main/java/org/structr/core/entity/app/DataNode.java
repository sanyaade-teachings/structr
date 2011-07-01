/*
 *  Copyright (C) 2011 Axel Morgner, structr <structr@structr.org>
 * 
 *  This file is part of structr <http://structr.org>.
 * 
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.structr.core.entity.app;

import org.structr.common.StructrOutputStream;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.ArbitraryNode;

/**
 *
 * @author Christian Morgner
 */
public class DataNode extends ArbitraryNode
{
	@Override
	public Object getProperty(String key)
	{
		return(super.getProperty(key));
	}

	@Override
	public void setProperty(String key, Object value)
	{
		super.setProperty(key, value);
	}

	@Override
	public String getIconSrc()
	{
		return("/images/database.png");
	}

    @Override
    public void onNodeCreation()
    {
    }

    @Override
    public void onNodeInstantiation()
    {
    }
}