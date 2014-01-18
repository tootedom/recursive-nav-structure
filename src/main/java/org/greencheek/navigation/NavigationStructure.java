package org.greencheek.navigation;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dominictootell
 * Date: 16/10/2013
 * Time: 19:46
 * To change this template use File | Settings | File Templates.
 */
public interface NavigationStructure {

//
//    Clothing -> Jackets -> Sport
//
    public Set<NavigationNode> getParents(String... navigationId);

}
