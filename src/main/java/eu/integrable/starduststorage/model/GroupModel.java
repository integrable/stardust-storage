package eu.integrable.starduststorage.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "db_group")
@Entity
public class GroupModel {
    @Column(nullable = false)
    @Id
    private String id;

    private String description;

    private String owner;

    private String permission;

    private Long quota;

    @Builder.Default
    private Long size = 0L;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    private Set<FileModel> files;

    @CreationTimestamp
    private LocalDateTime creationTime;

    @UpdateTimestamp
    private LocalDateTime modificationTime;

    public void increaseSize(Long size) {
        this.size += size;
    }

    public void decreaseSize(Long size) {
        this.size -= size;
    }
}