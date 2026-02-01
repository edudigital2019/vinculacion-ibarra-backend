package com.app.municipio.modules.business.controllers;

import com.app.municipio.modules.business.dto.Responses.BusinessCategorySelectDTO;
import com.app.municipio.modules.business.services.CategoryBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/businessCategories")
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoryBusinessService categoryBusinessService;

    @GetMapping("/select")
    public ResponseEntity<List<BusinessCategorySelectDTO>> getCategoriesForSelect() {
        return ResponseEntity.ok((categoryBusinessService.CategoriesSelect()));
    }
}
