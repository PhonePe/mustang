package com.phonepe.growth.mustang.detail.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonepe.growth.mustang.common.RequestContext;
import com.phonepe.growth.mustang.detail.Caveat;
import com.phonepe.growth.mustang.detail.Detail;
import com.phonepe.growth.mustang.detail.DetailVisitor;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EqualityInDetail extends Detail {

  @NotEmpty
  private final Set<Object> values;

  @Builder
  @JsonCreator
  public EqualityInDetail(@JsonProperty("values") Set<Object> values) {
    super(Caveat.EQUALITY_IN);
    this.values = values;
  }

  @Override
  public boolean validate(RequestContext context, Object lhsValue) {
    return values.contains(lhsValue);
  }

  @Override
  public <T> T accept(DetailVisitor<T> visitor) {
    return visitor.visit(this);
  }

}
