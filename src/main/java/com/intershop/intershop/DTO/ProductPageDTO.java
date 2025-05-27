package com.intershop.intershop.DTO;

import com.intershop.intershop.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class ProductPageDTO extends PageImpl<Product> {

    private final String search;

    public ProductPageDTO(
            List<Product> content,
            Pageable pageable,
            long total,
            String search) {
        super(content, pageable, total);
        this.search = search;
    }


    public String getSearch() {
        return search;
    }

}