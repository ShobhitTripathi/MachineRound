package com.getourguide.interview.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierSearchCriteria {
    private String name;       // partial match, case-insensitive
    private String address;    // partial match, case-insensitive
    private String zip;        // exact match
    private String city;       // partial match, case-insensitive
    private String country;    // partial match, case-insensitive
}
