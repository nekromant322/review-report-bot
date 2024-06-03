package com.nekromant.telegram.repository;

import com.nekromant.telegram.model.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Long> {
    public Role findByTitle(String admin);
}
