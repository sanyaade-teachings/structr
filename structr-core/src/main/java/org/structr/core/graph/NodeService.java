/**
 * Copyright (C) 2010-2014 Morgner UG (haftungsbeschränkt)
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.graph;

import org.apache.commons.collections.map.LRUMap;

import org.neo4j.gis.spatial.indexprovider.LayerNodeIndex;
import org.neo4j.gis.spatial.indexprovider.SpatialIndexProvider;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.impl.lucene.LuceneIndexImplementation;

import org.structr.core.Command;
import org.structr.core.RunnableService;
import org.structr.core.Services;
import org.structr.core.SingletonService;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.Location;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.shell.ShellSettings;
import org.structr.common.StructrConf;

//~--- classes ----------------------------------------------------------------

/**
 * The graph/node service main class.
 * 
 * @author Christian Morgner
 * @author Axel Morgner
 */
public class NodeService implements SingletonService {

	private static final Logger logger                       = Logger.getLogger(NodeService.class.getName());
	private static final Map<String, AbstractNode> nodeCache = (Map<String, AbstractNode>) Collections.synchronizedMap(new LRUMap(100000));

	//~--- fields ---------------------------------------------------------

	private GraphDatabaseService graphDb            = null;
	
	private Index<Node> caseInsensitiveUserIndex    = null;
	private Index<Node> fulltextIndex               = null;
	private Index<Node> keywordIndex                = null;
	private Index<Node> layerIndex                  = null;
	private Index<Node> userIndex                   = null;
	private Index<Node> uuidIndex                   = null;
	
	private Index<Relationship> relFulltextIndex    = null;
	private Index<Relationship> relKeywordIndex     = null;
	private Index<Relationship> relUuidIndex        = null;

	private ExecutionEngine cypherExecutionEngine   = null;
	
	// indices
	private Map<RelationshipIndex, Index<Relationship>> relIndices = new EnumMap<>(RelationshipIndex.class);
	private Map<NodeIndex, Index<Node>> nodeIndices                = new EnumMap<>(NodeIndex.class);

	/** Dependent services */
	private Set<RunnableService> registeredServices = new HashSet<>();
	private String filesPath                        = null;
	private boolean isInitialized                   = false;

	//~--- constant enums -------------------------------------------------

	/**
	 * The list of existing node indices.
	 */
	public static enum NodeIndex { uuid, user, caseInsensitiveUser, keyword, fulltext, layer }

	/**
	 * The list of existing relationship indices.
	 */
	public static enum RelationshipIndex { rel_uuid, rel_keyword, rel_fulltext }

	//~--- methods --------------------------------------------------------

	// <editor-fold defaultstate="collapsed" desc="interface SingletonService">
	@Override
	public void injectArguments(Command command) {

		if (command != null) {

			command.setArgument("graphDb", graphDb);
			
			command.setArgument(NodeIndex.uuid.name(), uuidIndex);
			command.setArgument(NodeIndex.fulltext.name(), fulltextIndex);
			command.setArgument(NodeIndex.user.name(), userIndex);
			command.setArgument(NodeIndex.caseInsensitiveUser.name(), caseInsensitiveUserIndex);
			command.setArgument(NodeIndex.keyword.name(), keywordIndex);
			command.setArgument(NodeIndex.layer.name(), layerIndex);
			
			command.setArgument(RelationshipIndex.rel_uuid.name(), relUuidIndex);
			command.setArgument(RelationshipIndex.rel_fulltext.name(), relFulltextIndex);
			command.setArgument(RelationshipIndex.rel_keyword.name(), relKeywordIndex);

			command.setArgument("filesPath", filesPath);
			
			command.setArgument("indices", NodeIndex.values());
			command.setArgument("relationshipIndices", RelationshipIndex.values());
			
			command.setArgument("cypherExecutionEngine", cypherExecutionEngine);
		}
	}

	@Override
	public void initialize(final StructrConf config) {

		final Map<String, String> neo4jConfiguration = new LinkedHashMap<>();
		final String basePath                        = config.getProperty(Services.BASE_PATH);
		final String dbPath                          = config.getProperty(Services.DATABASE_PATH);

		logger.log(Level.INFO, "Initializing database ({0}) ...", dbPath);

		if (graphDb != null) {

			logger.log(Level.INFO, "Database already running ({0}) ...", dbPath);

			return;

		}

		// neo4j remote shell configuration
		if ("true".equals(config.getProperty(Services.NEO4J_SHELL_ENABLED, "false"))) {
			
			// enable neo4j remote shell, thanks Michael :)
			neo4jConfiguration.put(ShellSettings.remote_shell_enabled.name(), "true");
		}
		
		
		try {

			graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbPath).setConfig(neo4jConfiguration).loadPropertiesFromFile(dbPath + "/neo4j.conf").newGraphDatabase();

		} catch (Throwable t) {

			logger.log(Level.INFO, "Database config {0}/neo4j.conf not found", dbPath);

			graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbPath).setConfig(neo4jConfiguration).newGraphDatabase();

		}

		if (graphDb == null) {

			logger.log(Level.SEVERE, "Database could not be started ({0}) ...", dbPath);

			return;

		}

		filesPath = config.getProperty(Services.FILES_PATH);

		// check existence of files path
		File files = new File(filesPath);

		if (!files.exists()) {

			files.mkdir();
		}

		logger.log(Level.INFO, "Database ready.");
		logger.log(Level.FINE, "Initializing UUID index...");

		uuidIndex = graphDb.index().forNodes("uuidAllNodes", LuceneIndexImplementation.EXACT_CONFIG);
		nodeIndices.put(NodeIndex.uuid, uuidIndex);

		logger.log(Level.FINE, "UUID index ready.");
		logger.log(Level.FINE, "Initializing user index...");

		userIndex = graphDb.index().forNodes("nameEmailAllUsers", LuceneIndexImplementation.EXACT_CONFIG);
		nodeIndices.put(NodeIndex.user, userIndex);

		logger.log(Level.FINE, "Node Email index ready.");
		logger.log(Level.FINE, "Initializing exact email index...");

		caseInsensitiveUserIndex = graphDb.index().forNodes("caseInsensitiveAllUsers", MapUtil.stringMap( "provider", "lucene", "type", "exact", "to_lower_case", "true" ));
		nodeIndices.put(NodeIndex.caseInsensitiveUser, caseInsensitiveUserIndex);

		logger.log(Level.FINE, "Node case insensitive node index ready.");
		logger.log(Level.FINE, "Initializing case insensitive fulltext node index...");

		fulltextIndex = graphDb.index().forNodes("fulltextAllNodes", LuceneIndexImplementation.FULLTEXT_CONFIG);
		nodeIndices.put(NodeIndex.fulltext, fulltextIndex);

		logger.log(Level.FINE, "Fulltext node index ready.");
		logger.log(Level.FINE, "Initializing keyword node index...");

		keywordIndex = graphDb.index().forNodes("keywordAllNodes", LuceneIndexImplementation.EXACT_CONFIG);
		nodeIndices.put(NodeIndex.keyword, keywordIndex);

		logger.log(Level.FINE, "Keyword node index ready.");
		logger.log(Level.FINE, "Initializing layer index...");

		final Map<String, String> spatialConfig = new HashMap<>();

		spatialConfig.put(LayerNodeIndex.LAT_PROPERTY_KEY, Location.latitude.dbName());
		spatialConfig.put(LayerNodeIndex.LON_PROPERTY_KEY, Location.longitude.dbName());
		spatialConfig.put(SpatialIndexProvider.GEOMETRY_TYPE, LayerNodeIndex.POINT_PARAMETER);

		layerIndex = new LayerNodeIndex("layerIndex", graphDb, spatialConfig);
		nodeIndices.put(NodeIndex.layer, layerIndex);

		logger.log(Level.FINE, "Layer index ready.");
		logger.log(Level.FINE, "Initializing node factory...");

		relUuidIndex = graphDb.index().forRelationships("uuidAllRelationships", LuceneIndexImplementation.EXACT_CONFIG);
		relIndices.put(RelationshipIndex.rel_uuid, relUuidIndex);

		logger.log(Level.FINE, "Relationship UUID index ready.");
		logger.log(Level.FINE, "Initializing relationship index...");

		relFulltextIndex = graphDb.index().forRelationships("fulltextAllRelationships", LuceneIndexImplementation.FULLTEXT_CONFIG);
		relIndices.put(RelationshipIndex.rel_fulltext, relFulltextIndex);

		logger.log(Level.FINE, "Relationship fulltext index ready.");
		logger.log(Level.FINE, "Initializing keyword relationship index...");

		relKeywordIndex = graphDb.index().forRelationships("keywordAllRelationships", LuceneIndexImplementation.EXACT_CONFIG);
		relIndices.put(RelationshipIndex.rel_keyword, relKeywordIndex);

		logger.log(Level.FINE, "Relationship numeric index ready.");
		logger.log(Level.FINE, "Initializing relationship factory...");

		logger.log(Level.FINE, "Relationship factory ready.");
		cypherExecutionEngine = new ExecutionEngine(graphDb);
		
		logger.log(Level.FINE, "Cypher execution engine ready.");
		
		isInitialized = true;
	}

	@Override
	public void shutdown() {

		if (isRunning()) {

			for (RunnableService s : registeredServices) {

				s.stopService();
			}

			// Wait for all registered services to end
			waitFor(registeredServices.isEmpty());
			graphDb.shutdown();

			graphDb       = null;
			isInitialized = false;

		}

	}

	public void registerService(final RunnableService service) {

		registeredServices.add(service);

	}

	public void unregisterService(final RunnableService service) {

		registeredServices.remove(service);

	}

	private void waitFor(final boolean condition) {

		while (!condition) {

			try {

				Thread.sleep(10);

			} catch (Throwable t) {}

		}

	}

	public static void addNodeToCache(String uuid, AbstractNode node) {

		nodeCache.put(uuid, node);

	}

	public static void removeNodeFromCache(String uuid) {

		nodeCache.remove(uuid);

	}

	//~--- get methods ----------------------------------------------------

	@Override
	public String getName() {

		return NodeService.class.getSimpleName();

	}

	public static AbstractNode getNodeFromCache(String uuid) {

		return nodeCache.get(uuid);

	}

	// </editor-fold>
	
	public GraphDatabaseService getGraphDb() {
		return graphDb;
	}
	
	@Override
	public boolean isRunning() {

		return ((graphDb != null) && isInitialized);
	}

	public Collection<Index<Node>> getNodeIndices() {
		return nodeIndices.values();
	}
	
	public Collection<Index<Relationship>> getRelationshipIndices() {
		return relIndices.values();
	}
	
	public Index<Node> getNodeIndex(NodeIndex name) {
		return nodeIndices.get(name);
	}
	
	public Index<Relationship> getRelationshipIndex(RelationshipIndex name) {
		return relIndices.get(name);
	}

}
