package org.greencheek.navigation;

/**
 * Created with IntelliJ IDEA.
 * User: dominictootell
 * Date: 16/10/2013
 * Time: 19:34
 * To change this template use File | Settings | File Templates.
 */
public class NavigiationNode {

    public final String name;
    public final String id;
    public final String parent;
    public final NavigiationNode parentReference;

    public NavigiationNode(String name, String id, String parent) {
        this(name,id,parent,null);
    }

    public NavigiationNode(String name, String id, String parent, NavigiationNode parentReference) {
        this.name = name;
        this.id = id;
        this.parent = parent;
        this.parentReference = parentReference;
    }

    public NavigiationNode linkToParent(final NavigiationNode parentReference) {
        if(parentReference==null) return this;
        return new NavigiationNode(this.name,this.id,this.parent,parentReference);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof NavigiationNode) {
            return ((NavigiationNode)o).id.equals(this.id);
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
