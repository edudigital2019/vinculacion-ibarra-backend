package com.app.municipio.modules.business.services;

import com.app.municipio.modules.business.dto.Responses.BusinessCategorySelectDTO;
import com.app.municipio.modules.business.repositories.BusinessCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryBusinessService {

    private final BusinessCategoryRepository categoryBusinessRepository;

    public List<BusinessCategorySelectDTO> CategoriesSelect() {
        return categoryBusinessRepository.findAll()
                .stream()
                .map(category -> BusinessCategorySelectDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .toList();
    }
}
