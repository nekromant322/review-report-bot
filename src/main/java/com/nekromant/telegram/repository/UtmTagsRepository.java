package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.UtmTags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtmTagsRepository extends JpaRepository<UtmTags, Long> {

    Optional<UtmTags> findByUtmSourceAndUtmMediumAndUtmContentAndUtmCampaign
            (String source, String medium, String content, String campaign);
}
