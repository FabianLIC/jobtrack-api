package com.fabianlicea.jobtrack.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fabianlicea.jobtrack.dto.ApplicationResponse;
import com.fabianlicea.jobtrack.dto.ApplicationRequest;
import com.fabianlicea.jobtrack.exceptions.ApplicationNotFoundException;
import com.fabianlicea.jobtrack.exceptions.UserNotFoundException;
import com.fabianlicea.jobtrack.repository.ApplicationRepository;
import com.fabianlicea.jobtrack.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import com.fabianlicea.jobtrack.model.Application;
import com.fabianlicea.jobtrack.model.User;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void findById_whenApplicationDoesNotExist_throwsException() {

        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ApplicationNotFoundException.class, () -> applicationService.findById(99L, 1L));
    }

    @Test
    void findById_whenApplicationExists_returnsResponse() {

        User u = new User("Alex", null, null);
        u.setId(1L);
        Application a = new Application("Indra", "Junior Development", null, null, null);
        a.setUser(u);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(a));

        ApplicationResponse result = applicationService.findById(1L, 1L);

        assertEquals("Indra", result.company());
    }

    @Test
    void findById_whenApplicationExists_belongsOtherUser() {

        User u = new User("Alex", null, null);
        u.setId(2L);
        Application a = new Application("Indra", "Junior Development", null, null, null);
        a.setUser(u);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(a));

        assertThrows(ApplicationNotFoundException.class, () -> applicationService.findById(1L, 1L));
    }

    @Test
    void fcreate_whenUserDoesNotExist_throwsException() {

        ApplicationRequest appRequest = new ApplicationRequest("Apple", "Mobile development", null, null, null, null,
                null, null, null, null);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> applicationService.create(appRequest, 99L));
    }

    @Test
    void delete_whenApplicationDoesNotExist_throwsException() {

        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ApplicationNotFoundException.class, () -> applicationService.delete(99L, 1L));
    }

    @Test
    void findAll_returnsUserApplications() {

        Application a1 = new Application("Indra", "Junior Development", null, null, null);
        Application a2 = new Application("Accenture", "Junior Development", null, null, null);

        when(applicationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(a1, a2));

        List<ApplicationResponse> result = applicationService.findAll(1L);

        assertEquals(2, result.size());
        assertEquals("Indra", result.get(0).company());
    }

}