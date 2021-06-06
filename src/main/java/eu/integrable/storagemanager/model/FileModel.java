package eu.integrable.storagemanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "FILE")
@Entity
public class FileModel {
    @Column(name = "ID", nullable = false)
    @Id
    private String id;

    @Column(nullable = false)
    private String filename;

    private String description;

    private String owner;

    private String permission;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String checksum;

    @CreationTimestamp
    private LocalDateTime creationTime;

    @UpdateTimestamp
    private LocalDateTime modificationTime;
}