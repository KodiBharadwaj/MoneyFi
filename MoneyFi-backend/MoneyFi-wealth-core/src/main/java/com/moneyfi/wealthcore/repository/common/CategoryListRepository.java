package com.moneyfi.wealthcore.repository.common;

import com.moneyfi.wealthcore.model.common.CategoryListModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryListRepository extends JpaRepository<CategoryListModel, Integer> {

    /** JPQL */
    @Query("SELECT c from CategoryListModel c WHERE c.type = :categoryType")
    List<CategoryListModel> findByType(String categoryType);

    /** JPQL */
    @Query("SELECT c FROM CategoryListModel c WHERE c.type IN :types")
    List<CategoryListModel> findByTypeIn(List<String> types);
}
