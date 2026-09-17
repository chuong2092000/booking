package project.commonutils.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import project.commonutils.entity.core.OutBoxCM;

import java.util.List;

@NoRepositoryBean
public interface OutboxRepoCM<T extends OutBoxCM> extends JpaRepository<T, String>, JpaSpecificationExecutor<T> {
    @Modifying
    @Query("UPDATE #{#entityName} o SET o.isSent = :status WHERE o.id IN :ids and o.isDeleted = false")
    int updateSentStatus(@Param("ids") List<String> ids, @Param("status") boolean status);
}