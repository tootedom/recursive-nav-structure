package org.greencheek.navigation;

import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: dominictootell
 * Date: 16/10/2013
 * Time: 19:40
 * To change this template use File | Settings | File Templates.
 */
public interface NavigtionStructureBuilder {
    public NavigationStructure build() throws IOException, SolrServerException;
    public NavigtionStructureBuilder add(NavigationNode node);
}
