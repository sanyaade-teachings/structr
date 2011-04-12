/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.structr.test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.structr.common.RelType;
import org.structr.common.StandaloneTestHelper;
import org.structr.core.Command;
import org.structr.core.Services;
import org.structr.core.cloud.PushNodes;
import org.structr.core.entity.AbstractNode;
import org.structr.core.entity.StructrRelationship;
import org.structr.core.entity.SuperUser;
import org.structr.core.node.CreateRelationshipCommand;
import org.structr.core.node.DeleteRelationshipCommand;
import org.structr.core.node.FindNodeCommand;
import org.structr.core.node.search.Search;
import org.structr.core.node.search.SearchNodeCommand;

/**
 *
 * @author axel
 */
public class RelinkContactPersons {

    public static void main(String[] args) {

        try {

            StandaloneTestHelper.prepareStandaloneTest("/opt/structr/t5s/db");

            //final AbstractNode adminNode = (AbstractNode) Services.command(FindUserCommand.class).execute("admin");

            //final String remoteServer = "localhost";
            //Services.command(PushNodes.class).execute(adminNode, remoteServer);

            List<AbstractNode> searchResult = (List<AbstractNode>) Services.command(SearchNodeCommand.class).execute(null, null, false, false, Search.andExactType("ContactPerson"));

            System.out.println("Found " + searchResult.size() + " contact persons");

            int i = 0;

            Command pushNodes = Services.command(PushNodes.class);
            Command findNode = Services.command(FindNodeCommand.class);

            AbstractNode rootNode = (AbstractNode) findNode.execute(new SuperUser(), 0L);

            String remoteHostValue = "true5stars.com";
            Integer tcpPort = 54555;
            Integer udpPort = 57555;
            boolean rec = false;

            Command createRel = Services.command(CreateRelationshipCommand.class);
            Command delRel = Services.command(DeleteRelationshipCommand.class);

            if (rootNode != null) {

                for (AbstractNode node : rootNode.getDirectChildNodes(new SuperUser())) {

                    Long hotelGroupId = node.getLongProperty("tmpHotelGroupId");

                    if (hotelGroupId != null) {

                        AbstractNode hotelGroup = (AbstractNode) findNode.execute(new SuperUser(), hotelGroupId);

                        createRel.execute(findNode.execute(new SuperUser(), 712L), node, RelType.HAS_CHILD);
                        System.out.println("Node " + node.getId() + " linked to Contact Persons root node");

                        if (hotelGroup != null && hotelGroupId > 0L) {

                            createRel.execute(node, hotelGroup, RelType.LINK);
                            System.out.println("Node " + node.getId() + " linked to hotel group " + hotelGroup.getName());

                        } else {
                            System.out.println("Hotel Group " + hotelGroup + " not found!");
                        }

                    }
                }


                System.out.println(i + " contact persons without CHILD relationship");
            }

            for (StructrRelationship r : rootNode.getOutgoingChildRelationships()) {

                if (r.getEndNode().getType().equals("ContactPerson")) {
                    delRel.execute(r);
                }

            }


            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RelinkContactPersons.class.getName()).log(Level.SEVERE, null, ex);
            }

            StandaloneTestHelper.finishStandaloneTest();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(RelinkContactPersons.class.getName()).log(Level.SEVERE, null, ex);
            }


        } catch (Exception e) {
            Logger.getLogger(RelinkContactPersons.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            StandaloneTestHelper.finishStandaloneTest();

        }
    }
}