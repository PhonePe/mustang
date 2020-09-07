package com.phonepe.growth.mustang.index;

import org.hibernate.validator.constraints.NotBlank;

import com.phonepe.growth.mustang.index.cnf.CNFInvertedList;
import com.phonepe.growth.mustang.index.dnf.DNFInvertedList;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndexGroup {
    @NotBlank
    private String name;
    private DNFInvertedList dnf;
    private CNFInvertedList cnf;

}
