package project.hotelservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.hotelservice.entity.Hotel;

import java.util.Optional;

@Repository
public interface HotelRepo extends JpaRepository<Hotel, String>, JpaSpecificationExecutor<Hotel> {

    Optional<Hotel> getHotelByIdAndIsDeletedAndIsApproval(String id, boolean isDeleted, boolean isApproval);

    Optional<Hotel> getHotelByIdAndIsDeleted(String id, boolean isDeleted);

    boolean existsByIdAndKeycloakIdAndIsDeleted(String id, String keycloakId, boolean isDeleted);

    Optional<Hotel> getHotelByIdAndKeycloakIdAndIsDeleted(String id, String keycloakId, boolean isDeleted);

    Optional<Hotel> findByIdAndImageByteIsNotNull(String id);

    @Modifying
    @Query("UPDATE Hotel h SET h.isDeleted = true WHERE h.id = :id and h.isDeleted = false")
    int deleteHotelById(@Param("id") String id);

}