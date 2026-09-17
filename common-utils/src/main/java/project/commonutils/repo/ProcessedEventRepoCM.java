package project.commonutils.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import project.commonutils.entity.core.ProcessedEventCM;

@NoRepositoryBean
public interface ProcessedEventRepoCM<T extends ProcessedEventCM> extends JpaRepository<T, String>, JpaSpecificationExecutor<T> {
    boolean existsByRefIdAndIsDeleted(String refId, Boolean isDeleted);
}