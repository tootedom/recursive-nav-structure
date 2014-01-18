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

    Map<String, NavigationNode> nodes = new HashMap<String,NavigationNode>();

    @Override
    public NavigationStructure build() {

        NavigationNode parent = new NavigationNode("Clothing","2","1");
        NavigationNode shoes = new NavigationNode("Shoes","3","1");

        NavigationNode shirts = new NavigationNode("Shirts","10","2");
        NavigationNode blouse = new NavigationNode("blouse","13","10");
        NavigationNode jackets = new NavigationNode("Jackets","11","2");
        NavigationNode sportsJacket = new NavigationNode("sportsJacket","12","11");

        NavigationNode boots = new NavigationNode("Boots","20","3");
        NavigationNode ankle = new NavigationNode("Ankle","41","20");
        NavigationNode knee = new NavigationNode("Knee","42","20");


        nodes.put("2",parent);
        nodes.put("10",shirts);
        nodes.put("13",blouse);
        nodes.put("11",jackets);
        nodes.put("12",sportsJacket);
        add(shoes).add(boots).add(ankle).add(knee);


        for(NavigationNode n : nodes.values()) {
            NavigationNode parentNode = nodes.get(n.parent);
            nodes.put(n.id,n.linkToParent(parentNode));
        }

        Map<String,Set<NavigationNode>> childParentsTreeStructure = new HashMap<String, Set<NavigationNode>>(nodes.size());

        for(NavigationNode n : nodes.values()) {
            childParentsTreeStructure.put(n.id, createParentHierachy(n, new LinkedHashSet<NavigationNode>(Collections.singletonList(n))));
        }

        return new MapBasedNavigationStructure(childParentsTreeStructure,nodes);
    }

    public NavigtionStructureBuilder add(NavigationNode node) {
        nodes.put(node.id,node);
        return this;
    }

    private Set<NavigationNode> createParentHierachy(NavigationNode n, Set<NavigationNode> visitedNodes) {
        String id = n.parent;
        if(id.equals("1")) return visitedNodes;
        if(visitedNodes.contains(id)) return visitedNodes;
        NavigationNode parent = nodes.get(id);
        if(parent == null) return visitedNodes;

        visitedNodes = new LinkedHashSet<NavigationNode>(visitedNodes);
        visitedNodes.add(parent);
        return createParentHierachy(nodes.get(id),visitedNodes);
    }


    public class MapBasedNavigationStructure implements NavigationStructure {

        Map<String,Set<NavigationNode>> nodes;
        Map<String,NavigationNode> allNodes;

        MapBasedNavigationStructure(Map<String,Set<NavigationNode>> navigationHierarchies,Map<String,NavigationNode> allNodes) {
            nodes = navigationHierarchies;
            this.allNodes = allNodes;
        }

        @Override
        public Set<NavigationNode> getParents(String... navigationId) {
            Set<NavigationNode> applicableNavigationNodes = new LinkedHashSet<NavigationNode>(navigationId.length*3,1);

            for(String nid : navigationId) {
                Set<NavigationNode> treeForId = nodes.get(nid);
                if(treeForId!=null) {
                    applicableNavigationNodes.addAll(treeForId);
                }
            }

            return applicableNavigationNodes;
        }

        public List<List<Set<NavigationNode>>> getChildren(String... navigationId) {
            List<List<Set<NavigationNode>>> childrenTree = new ArrayList<List<Set<NavigationNode>>>(navigationId.length);

            int i = 0;
            for(String topLevelId : navigationId) {
                List<Set<NavigationNode>> level =  new ArrayList<Set<NavigationNode>>();
                Set<NavigationNode> nodes = new LinkedHashSet<NavigationNode>();
                level.add(nodes);
                childrenTree.add(level);
                NavigationNode n = allNodes.get(topLevelId);
                if(n==null) continue;
                if(n.children.size()==0) continue;

                nodes.addAll(n.children.values());
                buildRows(n.children.values(), level);
            }

            return childrenTree;
        }


        private void buildRows(Collection<NavigationNode> children, List<Set<NavigationNode>> rows) {
            for(NavigationNode rowChild : children) {

                if(rowChild.children.size()>0) {
                    Set<NavigationNode> nodes = new LinkedHashSet<NavigationNode>();
                    nodes.addAll(rowChild.children.values());
                    rows.add(nodes);
                    buildRows(rowChild.children.values(),rows);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, SolrServerException {
        NavigtionStructureBuilder builder = new SimpleNavigationStructureBuilder();
        MapBasedNavigationStructure s = (MapBasedNavigationStructure)builder.build();

        System.out.println(s.getParents("13","20"));
        System.out.println(s.getChildren("2","3"));
    }
}
