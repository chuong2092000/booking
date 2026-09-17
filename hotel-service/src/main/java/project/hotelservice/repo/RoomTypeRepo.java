package project.hotelservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.hotelservice.entity.RoomType;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomTypeRepo extends JpaRepository<RoomType, String>, JpaSpecificationExecutor<RoomType> {
    @Modifying
    @Query("UPDATE RoomType h SET h.isDeleted = true WHERE h.hotelId = :hotelId and h.isDeleted = false")
    void deleteRoomTypeByHotelId(@Param("hotelId") String hotelId);

    @Modifying
    @Query("UPDATE RoomType h SET h.isDeleted = true WHERE h.id = :id and h.isDeleted = false")
    int deleteRoomTypeById(@Param("id") String id);

    @Modifying
    @Query("UPDATE RoomType h SET h.isDeleted = true WHERE h.id in (:ids) and h.isDeleted = false")
    int deleteRoomTypeByIds(@Param("ids") List<String> ids);

    @Query("SELECT h.id FROM RoomType h WHERE h.hotelId = :hotelId and h.isDeleted = false")
    List<String> findIdsByHotelId(@Param("hotelId") String hotelId);

    Optional<RoomType> getRoomTypeByIdAndIsDeletedAndIsApproval(String id, boolean isDeleted, boolean isApproval);

    Optional<RoomType> getRoomTypeByIdAndIsDeleted(String id, boolean isDeleted);

}
