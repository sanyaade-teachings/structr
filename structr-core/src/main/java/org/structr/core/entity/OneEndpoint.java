package org.structr.core.entity;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.structr.common.SecurityContext;
import org.structr.common.error.CardinalityToken;
import org.structr.common.error.FrameworkException;
import org.structr.core.Services;
import org.structr.core.graph.CreateRelationshipCommand;
import org.structr.core.graph.NodeFactory;
import org.structr.core.graph.NodeInterface;
import org.structr.core.graph.StructrTransaction;
import org.structr.core.graph.TransactionCommand;

/**
 *
 * @author Christian Morgner
 */
public class OneEndpoint<T extends NodeInterface> implements Target<Relationship, T> {
	
	private Relation<?, T, ?, OneEndpoint<T>> relation = null;
	
	public OneEndpoint(final Relation<?, T, ?, OneEndpoint<T>> relation) {
		this.relation = relation;
	}

	@Override
	public T get(final SecurityContext securityContext, final NodeInterface node) {
		
		final NodeFactory<T> nodeFactory = new NodeFactory<>(securityContext);
		final Relationship rel           = getRaw(node.getNode());
		
		if (rel != null) {
			return nodeFactory.adapt(rel.getEndNode());
		}
		
		return null;
	}

	@Override
	public void set(final SecurityContext securityContext, final NodeInterface sourceNode, final T targetNode) throws FrameworkException {
		
		Services.command(securityContext, TransactionCommand.class).execute(new StructrTransaction() {

			@Override
			public Object execute() throws FrameworkException {

				// let relation check multiplicity
				relation.checkMultiplicity(sourceNode, targetNode);
				
				// create new relationship
				Services.command(securityContext, CreateRelationshipCommand.class).execute(sourceNode, targetNode, relation.getClass());
				
				return null;
			}
			
		});
		
	}

	@Override
	public Relationship getRaw(Node dbNode) {
		return dbNode.getSingleRelationship(relation.getRelationshipType(), Direction.OUTGOING);
	}
}