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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.common.CurrentRequest;
import org.structr.common.CurrentSession;
import org.structr.common.RenderMode;
import org.structr.common.SessionValue;
import org.structr.core.NodeRenderer;
import org.structr.core.NodeSource;
import org.structr.core.Services;
import org.structr.core.entity.AbstractNode;
import org.structr.core.node.FindNodeCommand;
import org.structr.core.renderer.NodeLoaderRenderer;

/**
 *
 * @author Christian Morgner
 */
public class AppNodeLoader extends AbstractNode implements NodeSource
{
	public static final Logger logger = Logger.getLogger(AppNodeLoader.class.getName());

	public static final String ID_SOURCE_KEY = "idSource";

	// ----- instance variables -----
	private SessionValue<Object> sessionValue = null;

	@Override
	public String getIconSrc()
	{
		return ("/images/brick_edit.png");
	}

	@Override
	public void initializeRenderers(Map<RenderMode, NodeRenderer> renderers)
	{
		renderers.put(RenderMode.Default, new NodeLoaderRenderer());
	}

	@Override
	public void onNodeCreation()
	{
	}

	@Override
	public void onNodeInstantiation()
	{
	}

	@Override
	public void onNodeDeletion()
	{
	}

	public Object getValue()
	{
		String loaderSourceParameter = (String)getProperty(ID_SOURCE_KEY);
		Object value = CurrentRequest.getRequest().getParameter(loaderSourceParameter);

		if(CurrentSession.isRedirected())
		{
			value = getLastValue().get();

		} else
		{
			// otherwise, clear value in session
			getLastValue().set(value);
		}

		return (value);
	}

	public SessionValue<Object> getLastValue()
	{
		if(sessionValue == null)
		{
			sessionValue = new SessionValue<Object>(createUniqueIdentifier("lastValue"));
		}

		return (sessionValue);
	}

	// ----- interface NodeSource -----
	@Override
	public AbstractNode loadNode()
	{
		Object loaderValue = getValue();
		if(loaderValue != null)
		{
			return ((AbstractNode)Services.command(FindNodeCommand.class).execute(null, this, loaderValue));
		}

		return(null);
	}
}