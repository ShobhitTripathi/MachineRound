package com.getourguide.interview.dto.search;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivitySearchCriteria {
    private String title;
    private Integer minPrice;
    private Integer maxPrice;
    private Double minRating;
    private String currency;
    private Boolean specialOffer;
    private String supplierName;

    public boolean isEmpty() {
        return title == null
                && minPrice == null
                && maxPrice == null
                && minRating == null
                && currency == null
                && specialOffer == null
                && supplierName == null;
    }
}
