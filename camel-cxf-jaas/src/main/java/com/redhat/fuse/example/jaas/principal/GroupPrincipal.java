package com.redhat.fuse.example.jaas.principal;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Hashtable;

public class GroupPrincipal implements Group {

    private String name;
    private Hashtable<String,Principal> members = new Hashtable<String, Principal>();

    public GroupPrincipal(String name) {
        this.name = name;
    }

    public boolean addMember(Principal user) {
        members.put(user.getName(), user);
        return true;
    }

    public boolean removeMember(Principal user) {
        members.remove(user.getName());
        return true;
    }

    public boolean isMember(Principal member) {
        return members.contains(member.getName());
    }

    public Enumeration<? extends Principal> members() {
        return members.elements();
    }

    public String getName() {
        return name;
    }

}
