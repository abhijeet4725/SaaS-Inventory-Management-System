package com.saasproject.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Common pagination request DTO.
 */
@Data
public class PageRequestDto {

    @Min(0)
    private int page = 0;

    @Min(1)
    @Max(100)
    private int size = 20;

    private String sortBy = "createdAt";
    private String sortDir = "desc";

    public Pageable toPageable() {
        Sort sort = Sort.by(
                Sort.Direction.fromString(sortDir),
                sortBy);
        return PageRequest.of(page, size, sort);
    }

    public static Pageable defaultPageable() {
        return PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
