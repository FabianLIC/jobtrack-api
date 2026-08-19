package com.fabianlicea.jobtrack.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Note() {
    }

    public Note(String content, Application application) {
        this.content = content;
        this.application = application;
    }

    //GETTERS

    public String getContent() {
        return content;
    }

    public Application getApplication() {
        return application;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    //SETTERS

    public void setApplication(Application application) {
        this.application = application;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
