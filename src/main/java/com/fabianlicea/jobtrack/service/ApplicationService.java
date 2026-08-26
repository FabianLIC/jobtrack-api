package com.fabianlicea.jobtrack.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fabianlicea.jobtrack.dto.ApplicationRequest;
import com.fabianlicea.jobtrack.dto.ApplicationResponse;
import com.fabianlicea.jobtrack.exceptions.ApplicationNotFoundException;
import com.fabianlicea.jobtrack.exceptions.UserNotFoundException;
import com.fabianlicea.jobtrack.model.Application;
import com.fabianlicea.jobtrack.model.ApplicationStatus;
import com.fabianlicea.jobtrack.model.User;
import com.fabianlicea.jobtrack.repository.ApplicationRepository;
import com.fabianlicea.jobtrack.repository.UserRepository;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll(Long userId) {

        return applicationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(app -> toResponse(app))
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id, Long userId) {
        Application a = checkIdUser(id, userId);
        return toResponse(a);
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequest request, Long userId) {

        User u = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Application a = toEntity(request);
        a.setUser(u);
        applicationRepository.save(a);

        return toResponse(a);
    }

    @Transactional
    public ApplicationResponse update(Long id, Long userId, ApplicationRequest data) {
        Application application = checkIdUser(id, userId);

        application.setCompany(data.company());
        application.setPosition(data.position());
        application.setStatus(data.status());
        application.setLocation(data.location());
        application.setWorkMode(data.workMode());
        application.setSalaryMin(data.salaryMin());
        application.setSalaryMax(data.salaryMax());
        application.setOfferUrl(data.offerUrl());
        application.setSource(data.source());
        application.setAppliedAt(data.appliedAt());
        applicationRepository.saveAndFlush(application);

        return toResponse(application);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Application application = checkIdUser(id, userId);
        applicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findByUserIdAndStatus(Long userId, ApplicationStatus status) {

        return applicationRepository.findByUserIdAndStatus(userId, status).stream().map(app -> toResponse(app))
                .toList();
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
                application.getUpdatedAt());
    }

    private Application toEntity(ApplicationRequest request) {

        Application application = new Application();
        application.setCompany(request.company());
        application.setPosition(request.position());
        if (request.status() != null) {
            application.setStatus(request.status());
        }
        application.setLocation(request.location());
        application.setWorkMode(request.workMode());
        application.setSalaryMin(request.salaryMin());
        application.setSalaryMax(request.salaryMax());
        application.setOfferUrl(request.offerUrl());
        application.setSource(request.source());
        application.setAppliedAt(request.appliedAt());

        return application;
    }

    private Application checkIdUser(Long id, Long userId) {
        Application a = applicationRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
        if (!a.getUser().getId().equals(userId)) {
            throw new ApplicationNotFoundException(id);
        }
        return a;
    }

}
