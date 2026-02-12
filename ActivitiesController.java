package com.getourguide.interview.controller;

import com.getourguide.interview.dto.ActivityDto;
import com.getourguide.interview.dto.search.ActivitySearchCriteria;
import com.getourguide.interview.entity.Activity;
import com.getourguide.interview.entity.Supplier;
import com.getourguide.interview.service.ActivityService;
import com.getourguide.interview.service.search.SearchService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class ActivitiesController {

    private final ActivityService activityService;
    @Qualifier("activitySearchService")
    private final SearchService<Activity, ActivityDto, ActivitySearchCriteria> searchService;

    @GetMapping("/activities")
    public ResponseEntity<List<ActivityDto>> activities() {
        return ResponseEntity.ok(activityService.getActivities());
    }

    @GetMapping("/activities/{id}")
    public ResponseEntity<ActivityDto> activities(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivities(id));
    }

    @GetMapping("/activities/search/{search}")
    public ResponseEntity<List<ActivityDto>> activitiesSearch(@PathVariable String search) {
        return ResponseEntity.ok(activityService.searchActivities(search));
    }

    @GetMapping("/activities/search")
    public ResponseEntity<List<ActivityDto>> searchActivities(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) Boolean specialOffer,
            @RequestParam(required = false) String supplierName
    ) {
        ActivitySearchCriteria criteria = ActivitySearchCriteria.builder()
                .title(title)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .currency(currency)
                .specialOffer(specialOffer)
                .supplierName(supplierName)
                .build();

        return ResponseEntity.ok(searchService.search(criteria));
    }
}
