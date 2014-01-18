package org.greencheek.navigation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dominictootell
 * Date: 16/10/2013
 * Time: 19:34
 * To change this template use File | Settings | File Templates.
 */
public class NavigationNode {

    public final String name;
    public final String id;
    public final String parent;
    public final NavigationNode parentReference;
    public final Map<String,NavigationNode> children = new HashMap<String, NavigationNode>();

    public NavigationNode(String name, String id, String parent) {
        this(name,id,parent,null);
    }

    public NavigationNode(String name, String id, String parent, NavigationNode parentReference) {
        this.name = name;
        this.id = id;
        this.parent = parent;
        this.parentReference = parentReference;
    }

    public NavigationNode linkToParent(final NavigationNode parentReference) {
        if(parentReference==null) return this;
        NavigationNode node = new NavigationNode(this.name,this.id,this.parent,parentReference);
        return addChildToParent(parentReference,node);
    }

    public NavigationNode addChildToParent(final NavigationNode parentReference, final NavigationNode childReference) {
        parentReference.children.put(childReference.id,childReference);
        return childReference;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof NavigationNode) {
            return ((NavigationNode)o).id.equals(this.id);
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder b = new StringBuilder(64);
        b.append("id:").append(id);
        b.append(",name:").append(name);
        b.append(",parentid:").append(parent);
        return b.toString();
    }

}
