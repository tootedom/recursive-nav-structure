package org.greencheek.navigation;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: dominictootell
 * Date: 16/10/2013
 * Time: 22:31
 * To change this template use File | Settings | File Templates.
 */
public class SolrJNavigationStructBuilder implements NavigtionStructureBuilder{


    @Override
    public NavigationStructure build() throws IOException, SolrServerException {
        HttpSolrServer solr = new HttpSolrServer("http://localhost:8000");

        ModifiableSolrParams params = new ModifiableSolrParams();
        params.add("wt","xml");
        params.add("qt","/navcatjava.bin");
        //params.add("q","*:*");
        params.add("response-streaming","true");
        params.add("fl","id");

        //System.out.println(solr.query( params ).getResponse());
        params.set("q","cat_s:2");


        ConcurrentHashMap<String,NavigationNode> navigationNodes = new ConcurrentHashMap<String,NavigationNode>();

        NavigationNodeCallback callback = new NavigationNodeCallback(navigationNodes);
        QueryResponse rsp = solr.queryAndStreamResponse(params, callback);
        System.out.println(navigationNodes.size());
        System.out.println(rsp.getElapsedTime());

        for(NavigationNode n : navigationNodes.values()) {
            NavigationNode parentNode = navigationNodes.get(n.parent);
            navigationNodes.put(n.id,n.linkToParent(parentNode));
        }

        Map<String,Set<NavigationNode>> childParentsTreeStructure = new HashMap<String, Set<NavigationNode>>(navigationNodes.size());

        for(NavigationNode n : navigationNodes.values()) {
            childParentsTreeStructure.put(n.id, createParentHierachy(navigationNodes,n, new LinkedHashSet<NavigationNode>(Collections.singletonList(n))));
        }

        return new MapBasedNavigationStructure(childParentsTreeStructure,navigationNodes);
    }

    @Override
    public NavigtionStructureBuilder add(NavigationNode node) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private Set<NavigationNode> createParentHierachy(Map<String,NavigationNode> navigationNodes,NavigationNode n, Set<NavigationNode> visitedNodes) {
        String id = n.parent;
        if(id.equals("1")) return visitedNodes;
        if(visitedNodes.contains(id)) return visitedNodes;
        NavigationNode parent = navigationNodes.get(id);
        if(parent == null) return visitedNodes;

        visitedNodes = new LinkedHashSet<NavigationNode>(visitedNodes);
        visitedNodes.add(parent);
        return createParentHierachy(navigationNodes,navigationNodes.get(id),visitedNodes);
    }


    public class MapBasedNavigationStructure implements NavigationStructure {
        Map<String,NavigationNode> allNodes;

        Map<String,Set<NavigationNode>> nodes;

        MapBasedNavigationStructure(Map<String,Set<NavigationNode>> navigationHierarchies,Map<String,NavigationNode> allNodes) {
            this.allNodes = allNodes;
            nodes = navigationHierarchies;
        }

        public int size() {
            return nodes.size();
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
        NavigtionStructureBuilder builder = new SolrJNavigationStructBuilder();
        MapBasedNavigationStructure s = (MapBasedNavigationStructure)builder.build();

        System.out.println(s.size());
        System.out.println(s.getParents("6333"));
        System.out.println(s.getChildren("43").get(0));
    }

    private static final class NavigationNodeCallback extends StreamingResponseCallback {

        private final Map<String,NavigationNode> navigiationNodeMap;

        NavigationNodeCallback(Map<String,NavigationNode> node) {
            navigiationNodeMap = node;
        }

        @Override
        public void streamSolrDocument(SolrDocument doc) {
            Object type = doc.getFieldValue("name_en");
            Object id = doc.getFieldValue("id");
            Object parent_id = doc.getFieldValue("parent_id");

            if(id == null ) {
                System.out.println("id is null:"+ doc.toString());
                return;
            }
            if(parent_id == null ) {
                System.out.println("parent_id is null:"+ doc.toString());
                return;
            }
            if(type == null ) {
//                System.out.println("name_en is null:"+ doc.toString());
                for(String ending : new String[]{"en","fr","it","zh","de"})
                {
                    type = doc.getFieldValue("name_"+ending);
                    if(type!=null) { break;}
                    else type="NONAME";
                }

            }


            NavigationNode n = new NavigationNode(type.toString(),id.toString(),parent_id.toString());
            navigiationNodeMap.put(n.id,n);
        }

        @Override
        public void streamDocListInfo(long numFound, long start, Float maxScore) {
        }


    }
}
