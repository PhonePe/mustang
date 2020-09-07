package com.phonepe.growth.mustang;

import java.util.List;

import com.phonepe.growth.mustang.common.EvaluationContext;
import com.phonepe.growth.mustang.criteria.Criteria;
import com.phonepe.growth.mustang.index.Indexer;
import com.phonepe.growth.mustang.scan.Scanner;
import com.phonepe.growth.mustang.search.Searcher;

public class MustangEngine {
    public void index(String index, List<Criteria> criterias) {
        Indexer.add(index, criterias);
    }

    public void index(String index, Criteria criteria) {
        Indexer.add(index, criteria);
    }

    public List<Criteria> search(String index, EvaluationContext context) {
        return Searcher.builder().index(index).context(context).build().search();
    }

    public List<Criteria> scan(List<Criteria> criterias, EvaluationContext context) {
        return Scanner.builder().criterias(criterias).context(context).build().scan();
    }

    public boolean evaluate(Criteria criteria, EvaluationContext context) {
        return criteria.evaluate(context);
    }

}
