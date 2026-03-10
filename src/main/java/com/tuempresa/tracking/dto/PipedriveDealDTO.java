package com.tuempresa.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PipedriveDealDTO {
    private String title;
    private String person_name;
    private String email;
    private Double value;
    private String currency;
    private String status;
}
