package com.getourguide.interview.service.search;

import java.util.List;

public interface SearchService<ENTITY, DTO, CRITERIA> {
    List<DTO> search(CRITERIA criteria);
    DTO findById(Long id);
    List<DTO> findAll();
}
