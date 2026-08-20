package com.fabianlicea.jobtrack.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fabianlicea.jobtrack.model.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

}
