/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.structr.schema;

import java.util.LinkedHashSet;
import java.util.Set;
import org.structr.common.StructrConf;
import org.structr.common.error.ErrorBuffer;
import org.structr.core.Command;
import org.structr.core.Service;
import org.structr.core.Services;
import org.structr.core.app.StructrApp;
import org.structr.core.entity.SchemaNode;
import org.structr.core.entity.relationship.SchemaRelationship;
import org.structr.schema.compiler.NodeExtender;

/**
 *
 * @author Christian Morgner
 */
public class SchemaService implements Service {

	@Override
	public void injectArguments(final Command command) {
	}

	@Override
	public void initialize(final StructrConf config) {
		reloadSchema(new ErrorBuffer());
	}
	
	public static boolean reloadSchema(final ErrorBuffer errorBuffer) {

		final Set<String> dynamicViews  = new LinkedHashSet<>();
		final NodeExtender nodeExtender = new NodeExtender();
		boolean success                 = true;

		try {
			// collect node classes
			for (final SchemaNode schemaNode : StructrApp.getInstance().nodeQuery(SchemaNode.class).getAsList()) {
				nodeExtender.addClass(schemaNode.getClassName(), schemaNode.getSource(errorBuffer));
				dynamicViews.addAll(schemaNode.getViews());
			}

			// collect relationship classes
			for (final SchemaRelationship schemaRelationship : StructrApp.getInstance().relationshipQuery(SchemaRelationship.class).getAsList()) {
				nodeExtender.addClass(schemaRelationship.getClassName(), schemaRelationship.getSource(errorBuffer));
				dynamicViews.addAll(schemaRelationship.getViews());
			}

			// compile all classes at once
			for (final Class newType : nodeExtender.compile(errorBuffer).values()) {
				Services.getInstance().getConfigurationProvider().registerEntityType(newType);
			}

			success = !errorBuffer.hasError();
			
			// inject views in configuration provider
			if (success) {
				
				Services.getInstance().getConfigurationProvider().registerDynamicViews(dynamicViews);
				// TODO: add all views 
			}
			
		} catch (Throwable t) {
			
			success = false;
		}
		
		return success;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public String getName() {
		return SchemaService.class.getName();
	}

	@Override
	public boolean isRunning() {
		return true;
	}
}
