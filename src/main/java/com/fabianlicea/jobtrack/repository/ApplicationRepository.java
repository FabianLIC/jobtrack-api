package com.fabianlicea.jobtrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fabianlicea.jobtrack.model.Application;
import com.fabianlicea.jobtrack.model.ApplicationStatus;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    List<Application> findByUserIdAndCompanyContainingIgnoreCase(Long userId, String company);

}
