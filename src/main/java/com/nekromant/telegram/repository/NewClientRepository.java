package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.NewClient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewClientRepository extends CrudRepository<NewClient, Long> {
}
