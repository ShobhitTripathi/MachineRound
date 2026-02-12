package com.getourguide.interview.service.search;

import com.getourguide.interview.dto.ActivityDto;
import com.getourguide.interview.dto.search.ActivitySearchCriteria;
import com.getourguide.interview.entity.Activity;
import com.getourguide.interview.repository.ActivityRepository;
import com.getourguide.interview.specification.ActivitySpecifications;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ActivitySearchService implements SearchService<Activity, ActivityDto, ActivitySearchCriteria> {

    private final ActivityRepository activityRepository;

    @Override
    public List<ActivityDto> search(ActivitySearchCriteria criteria) {
        // Build specification from criteria
        Specification<Activity> spec = ActivitySpecifications.fromCriteria(criteria);

        // Execute query (database-level filtering)
        List<Activity> activities = activityRepository.findAll(spec);

        // Map to DTOs
        return mapToDtoList(activities);
    }

    @Override
    public ActivityDto findById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));
        return mapToDto(activity);
    }

    @Override
    public List<ActivityDto> findAll() {
        List<Activity> activities = activityRepository.findAll();
        return mapToDtoList(activities);
    }

    /**
     * Map single Activity entity to DTO
     * Private helper - keeps mapping logic in one place
     */
    private ActivityDto mapToDto(Activity activity) {
        return ActivityDto.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .price(activity.getPrice())
                .currency(activity.getCurrency())
                .rating(activity.getRating())
                .specialOffer(activity.isSpecialOffer())
                .supplierName(activity.getSupplier() != null ? activity.getSupplier().getName() : "")
                .build();
    }

    private List<ActivityDto> mapToDtoList(List<Activity> activities) {
        return activities.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
