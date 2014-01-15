package org.greencheek.navigation;

import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: dominictootell
 * Date: 16/10/2013
 * Time: 21:56
 * To change this template use File | Settings | File Templates.
 */
public class SimpleNavigationStructureBuilder implements NavigtionStructureBuilder{

    Map<String, NavigiationNode> nodes = new HashMap<String,NavigiationNode>();

    @Override
    public NavigationStructure build() {

        NavigiationNode parent = new NavigiationNode("Clothing","2","1");
        NavigiationNode shoes = new NavigiationNode("Shoes","3","1");

        NavigiationNode shirts = new NavigiationNode("Shirts","10","2");
        NavigiationNode blouse = new NavigiationNode("blouse","13","10");
        NavigiationNode jackets = new NavigiationNode("Jackets","11","2");
        NavigiationNode sportsJacket = new NavigiationNode("sportsJacket","12","11");

        NavigiationNode boots = new NavigiationNode("Boots","20","3");
        NavigiationNode ankle = new NavigiationNode("Ankle","41","20");
        NavigiationNode knee = new NavigiationNode("Knee","42","20");


        nodes.put("2",parent);
        nodes.put("10",shirts);
        nodes.put("13",blouse);
        nodes.put("11",jackets);
        nodes.put("12",sportsJacket);
        add(shoes).add(boots).add(ankle).add(knee);


        for(NavigiationNode n : nodes.values()) {
            NavigiationNode parentNode = nodes.get(n.parent);
            nodes.put(n.id,n.linkToParent(parentNode));
        }

        Map<String,Set<NavigiationNode>> childParentsTreeStructure = new HashMap<String, Set<NavigiationNode>>(nodes.size());

        for(NavigiationNode n : nodes.values()) {
            childParentsTreeStructure.put(n.id, createParentHierachy(n, new LinkedHashSet<NavigiationNode>(Collections.singletonList(n))));
        }

        return new MapBasedNavigationStructure(childParentsTreeStructure);
    }

    public NavigtionStructureBuilder add(NavigiationNode node) {
        nodes.put(node.id,node);
        return this;
    }

    private Set<NavigiationNode> createParentHierachy(NavigiationNode n, Set<NavigiationNode> visitedNodes) {
        String id = n.parent;
        if(id.equals("1")) return visitedNodes;
        if(visitedNodes.contains(id)) return visitedNodes;
        NavigiationNode parent = nodes.get(id);
        if(parent == null) return visitedNodes;

        visitedNodes = new LinkedHashSet<NavigiationNode>(visitedNodes);
        visitedNodes.add(parent);
        return createParentHierachy(nodes.get(id),visitedNodes);
    }


    public class MapBasedNavigationStructure implements NavigationStructure {

        Map<String,Set<NavigiationNode>> nodes;

        MapBasedNavigationStructure(Map<String,Set<NavigiationNode>> navigationHierarchies) {
            nodes = navigationHierarchies;
        }

        @Override
        public Set<NavigiationNode> getParents(String... navigationId) {
            Set<NavigiationNode> applicableNavigationNodes = new LinkedHashSet<NavigiationNode>(navigationId.length*3,1);

            for(String nid : navigationId) {
                Set<NavigiationNode> treeForId = nodes.get(nid);
                if(treeForId!=null) {
                    applicableNavigationNodes.addAll(treeForId);
                }
            }

            return applicableNavigationNodes;
        }
    }

    public static void main(String[] args) throws IOException, SolrServerException {
        NavigtionStructureBuilder builder = new SimpleNavigationStructureBuilder();
        NavigationStructure s = builder.build();

        System.out.println(s.getParents("13","20"));

    }
}
