package com.bornfire.brf.entities;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BRF_052_ARCHIVE_INDEX_Repo
        extends JpaRepository<BRF_052_ARCHIVE_INDEX_Entity, Date> {

    // Returns all archive entries newest first — powers the Archive list page
    List<BRF_052_ARCHIVE_INDEX_Entity> findAllByOrderByReportDateDesc();
}