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


        ConcurrentHashMap<String,NavigiationNode> navigationNodes = new ConcurrentHashMap<String,NavigiationNode>();

        NavigationNodeCallback callback = new NavigationNodeCallback(navigationNodes);
        QueryResponse rsp = solr.queryAndStreamResponse(params, callback);
        System.out.println(navigationNodes.size());
        System.out.println(rsp.getElapsedTime());

        for(NavigiationNode n : navigationNodes.values()) {
            NavigiationNode parentNode = navigationNodes.get(n.parent);
            navigationNodes.put(n.id,n.linkToParent(parentNode));
        }

        Map<String,Set<NavigiationNode>> childParentsTreeStructure = new HashMap<String, Set<NavigiationNode>>(navigationNodes.size());

        for(NavigiationNode n : navigationNodes.values()) {
            childParentsTreeStructure.put(n.id, createParentHierachy(navigationNodes,n, new LinkedHashSet<NavigiationNode>(Collections.singletonList(n))));
        }

        return new MapBasedNavigationStructure(childParentsTreeStructure);
    }

    @Override
    public NavigtionStructureBuilder add(NavigiationNode node) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private Set<NavigiationNode> createParentHierachy(Map<String,NavigiationNode> navigationNodes,NavigiationNode n, Set<NavigiationNode> visitedNodes) {
        String id = n.parent;
        if(id.equals("1")) return visitedNodes;
        if(visitedNodes.contains(id)) return visitedNodes;
        NavigiationNode parent = navigationNodes.get(id);
        if(parent == null) return visitedNodes;

        visitedNodes = new LinkedHashSet<NavigiationNode>(visitedNodes);
        visitedNodes.add(parent);
        return createParentHierachy(navigationNodes,navigationNodes.get(id),visitedNodes);
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
        NavigtionStructureBuilder builder = new SolrJNavigationStructBuilder();
        NavigationStructure s = builder.build();

        System.out.println(s.getParents("6333"));

    }

    private static final class NavigationNodeCallback extends StreamingResponseCallback {

        private final Map<String,NavigiationNode> navigiationNodeMap;

        NavigationNodeCallback(Map<String,NavigiationNode> node) {
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
                System.out.println("name_en is null:"+ doc.toString());
                type="NONAME";
            }


            NavigiationNode n = new NavigiationNode(type.toString(),id.toString(),parent_id.toString());
            navigiationNodeMap.put(n.id,n);
        }

        @Override
        public void streamDocListInfo(long numFound, long start, Float maxScore) {
        }


    }
}
