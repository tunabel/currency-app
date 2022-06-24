package com.gable.currencyapp.dto.gecko;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DescriptionDto {

  @JsonProperty("en")
  private String en;

}
