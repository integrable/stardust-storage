package eu.integrable.starduststorage.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "db_file")
@Entity
public class FileModel {
    @Column(nullable = false)
    @Id
    private String id;

    @Column(nullable = false)
    private String filename;

    private String description;

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private GroupModel group;

    private String owner;

    private String permission;

    @Column(nullable = false)
    private String mediaType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String checksum;

    @CreationTimestamp
    private LocalDateTime creationTime;

    @UpdateTimestamp
    private LocalDateTime modificationTime;
}