package eu.integrable.starduststorage.repository;

import eu.integrable.starduststorage.model.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileModelRepository extends JpaRepository<FileModel, String> {
}