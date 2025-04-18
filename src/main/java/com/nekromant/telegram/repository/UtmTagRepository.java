package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.UtmTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtmTagRepository extends JpaRepository<UtmTag, Long> {

    Optional<UtmTag> getUtmTagsBySourceAndSection(String source, String section);
}
