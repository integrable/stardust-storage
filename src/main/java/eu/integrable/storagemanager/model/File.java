package eu.integrable.storagemanager.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;


@Data
@Table(name = "FILE")
@Entity
public class File {
    @Column(name = "ID", nullable = false)
    @Id
    private String id;

    @Column(nullable = false)
    private String filename;

    private String description;

    private String owner;

    private String permission;

    @Column(nullable = false)
    private String sha256;

    @CreationTimestamp
    private LocalDateTime creationTime;

    @UpdateTimestamp
    private LocalDateTime modificationTime;
}