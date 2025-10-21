package com.phonepe.central.mustang.service;

import java.util.List;
import java.util.Set;

import com.phonepe.central.mustang.request.ScanCriteriaRequest;
import com.phonepe.central.mustang.request.ScanIndexRequest;
import com.phonepe.growth.mustang.criteria.Criteria;

public interface ScanService {

    Set<String> scan(final ScanIndexRequest request);

    List<Criteria> scan(final ScanCriteriaRequest request);

}
