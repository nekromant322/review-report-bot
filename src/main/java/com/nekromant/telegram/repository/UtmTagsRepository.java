package com.nekromant.telegram.repository;

import com.nekromant.telegram.dto.UtmDTO;
import com.nekromant.telegram.model.UtmTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtmTagsRepository extends JpaRepository<UtmTags, Long> {

    Optional<UtmTags> findByUtmSourceAndUtmMediumAndUtmContentAndUtmCampaign(
            String utmSource,
            String utmMedium,
            String utmContent,
            String utmCampaign
    );

    default Optional<UtmTags> findByDto(UtmDTO dto) {
        return findByUtmSourceAndUtmMediumAndUtmContentAndUtmCampaign(
                dto.getUtmSource(),
                dto.getUtmMedium(),
                dto.getUtmContent(),
                dto.getUtmCampaign()
        );
    }
}
