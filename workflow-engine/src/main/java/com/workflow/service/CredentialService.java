package com.workflow.service;

import com.workflow.model.Credential;
import com.workflow.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * 凭证管理服务 (类似 n8n 的 Credentials 管理)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CredentialService {

    private final CredentialRepository credentialRepository;

    public Credential createCredential(Credential credential) {
        if (credentialRepository.existsByName(credential.getName())) {
            throw new IllegalArgumentException("Credential name already exists: " + credential.getName());
        }
        return credentialRepository.save(credential);
    }

    public Credential updateCredential(Long id, Credential updated) {
        Credential existing = credentialRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Credential not found: " + id));
        
        existing.setName(updated.getName());
        existing.setType(updated.getType());
        existing.setData(updated.getData());
        existing.setDescription(updated.getDescription());
        
        return credentialRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Optional<Credential> getCredential(Long id) {
        return credentialRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Credential> getCredentialByName(String name) {
        return credentialRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Credential> getAllCredentials() {
        return credentialRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Credential> getCredentialsByType(String type) {
        return credentialRepository.findByType(type);
    }

    public void deleteCredential(Long id) {
        credentialRepository.deleteById(id);
    }
}
