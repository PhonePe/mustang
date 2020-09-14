package com.phonepe.growth.mustang.index;

import org.hibernate.validator.constraints.NotBlank;

import com.phonepe.growth.mustang.index.cnf.CNFInvertedIndex;
import com.phonepe.growth.mustang.index.dnf.DNFInvertedIndex;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndexGroup {
    @NotBlank
    private String name;
    private DNFInvertedIndex dnf;
    private CNFInvertedIndex cnf;

}
