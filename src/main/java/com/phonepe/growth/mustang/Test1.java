package com.phonepe.growth.mustang;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.phonepe.growth.mustang.index.core.DisjunctionPostingEntry;
import com.phonepe.growth.mustang.predicate.PredicateType;

public class Test1 {
    public static void main(String[] args) {
        final DisjunctionPostingEntry postingEntry = DisjunctionPostingEntry.builder()
                .iId(2)
                .eId("C2")
                .type(PredicateType.INCLUDED)
                .order(0)
                .score(0)
                .build();
        
        Set<DisjunctionPostingEntry> set = Sets.newTreeSet();
        set.add(postingEntry);
        
        
        final DisjunctionPostingEntry postingEntry1 = DisjunctionPostingEntry.builder()
                .iId(1)
                .eId("A")
                .type(PredicateType.INCLUDED)
                .order(1)
                .score(0)
                .build();
        //set.add(postingEntry1);
        System.out.println(set);
        System.out.println(set.contains(postingEntry1));
        
        
        Map<Integer, DisjunctionPostingEntry> map = Maps.newTreeMap();
        map.put(postingEntry.getIId(), postingEntry);
        System.out.println(map.values().contains(postingEntry));
        System.out.println(map.values().contains(postingEntry1));
        
        
    }
}
