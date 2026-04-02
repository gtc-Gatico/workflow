package com.workflow.repository;

import com.workflow.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Optional<Credential> findByName(String name);
    List<Credential> findByType(String type);
    boolean existsByName(String name);
}
