package eu.integrable.storagemanager.repository;

import eu.integrable.storagemanager.model.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileModelRepository extends JpaRepository<FileModel, String> {
}