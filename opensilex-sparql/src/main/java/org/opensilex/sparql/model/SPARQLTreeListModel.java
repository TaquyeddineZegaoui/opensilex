/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opensilex.sparql.model;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 *
 * @author vince
 */
public class SPARQLTreeListModel<T extends SPARQLTreeModel<T>> {

    private final HashMap<URI, Set<T>> map = new HashMap<URI, Set<T>>();

    private final List<URI> selectionList = new LinkedList<>();
    private final URI root;
    private final boolean excludeRoot;

    public SPARQLTreeListModel() {
        this(new LinkedList<>(), null, false);
    }

    public SPARQLTreeListModel(List<T> selectionList, URI root, boolean excludeRoot) {
        for (T instance : selectionList) {
            this.selectionList.add(instance.getUri());
        }
        this.root = root;
        this.excludeRoot = excludeRoot;
    }

    public void listRoots(Consumer<T> handler) {
        this.listChildren(null, handler);
    }

    public void listChildren(T parent, Consumer<T> handler) {
        URI parentURI = null;
        if (parent != null) {
            parentURI = parent.getUri();
        }
        if (this.map.containsKey(parentURI)) {
            this.map.get(parentURI).forEach(handler);
        }
    }

    public void addTree(T candidate) {
        if (!map.containsKey(candidate.getUri())) {

            T parent = (T) candidate.getParent();

            if (parent == null || candidate.getUri().equals(root)) {
                if (!excludeRoot) {
                    if (!map.containsKey(null)) {
                        map.put(null, new HashSet<T>());
                    }
                    map.get(null).add(candidate);
                }
            } else {
                URI parentURI = parent.getUri();
                if (parentURI.equals(root) && excludeRoot) {
                    parentURI = null;
                }
                if (!map.containsKey(parentURI)) {
                    addTree(parent);
                    map.put(parentURI, new HashSet<T>());
                }

                map.get(parentURI).add(candidate);
            }

        }
    }

    public boolean isSelected(T candidate) {
        return this.selectionList.contains(candidate.getUri());
    }
}
