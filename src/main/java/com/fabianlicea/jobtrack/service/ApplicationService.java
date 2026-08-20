package com.fabianlicea.jobtrack.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabianlicea.jobtrack.dto.ApplicationResponse;
import com.fabianlicea.jobtrack.exceptions.ApplicationNotFoundException;
import com.fabianlicea.jobtrack.model.Application;
import com.fabianlicea.jobtrack.model.ApplicationStatus;
import com.fabianlicea.jobtrack.repository.ApplicationRepository;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {

        return applicationRepository.findAll().stream().map(app -> toResponse(app)).toList();
    }

    @Transactional(readOnly = true)
    public Application findById(Long id) {
        return applicationRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    @Transactional
    public Application create(Application application) {
        return applicationRepository.save(application);
    }

    @Transactional
    public Application update(Long id, Application data) {
        Application application = findById(id);

        application.setCompany(data.getCompany());
        application.setPosition(data.getPosition());
        application.setStatus(data.getStatus());
        application.setLocation(data.getLocation());
        application.setWorkMode(data.getWorkMode());
        application.setSalaryMin(data.getSalaryMin());
        application.setSalaryMax(data.getSalaryMax());
        application.setOfferUrl(data.getOfferUrl());
        application.setSource(data.getSource());
        application.setAppliedAt(data.getAppliedAt());

        return applicationRepository.save(application);
    }

    @Transactional
    public void delete(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ApplicationNotFoundException(id);
        }
        applicationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status) {

        return applicationRepository.findByUserIdAndStatus(userId, status);
    }

    private ApplicationResponse toResponse(Application application) {
    return new ApplicationResponse(
        application.getId(),
        application.getCompany(),
        application.getPosition(),
        application.getStatus(),
        application.getLocation(),
        application.getWorkMode(),
        application.getSalaryMin(),
        application.getSalaryMax(),
        application.getOfferUrl(),
        application.getSource(),
        application.getAppliedAt(),
        application.getCreatedAt(),
        application.getUpdatedAt()
    );
}

}
