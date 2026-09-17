package project.hotelservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.hotelservice.entity.RoomTypeImage;

import java.util.List;

@Repository
public interface RoomTypeImageRepo extends JpaRepository<RoomTypeImage, String>, JpaSpecificationExecutor<RoomTypeImage> {
    @Modifying
    @Query("UPDATE RoomTypeImage h SET h.isDeleted = true WHERE h.roomTypeId in (:roomTypeIds) and h.isDeleted = false")
    void deleteRoomTypeImageByRoomTypeIds(@Param("roomTypeIds") List<String> roomTypeIds);

    @Modifying
    @Query("UPDATE RoomTypeImage h SET h.isDeleted = true WHERE h.roomTypeId = :roomTypeId and h.isDeleted = false")
    void deleteRoomTypeImageByRoomTypeId(@Param("roomTypeId") String roomTypeId);

    @Modifying
    @Query("UPDATE RoomTypeImage h SET h.isDeleted = true WHERE h.id = :id and h.isDeleted = false")
    int deleteRoomTypeImageById(@Param("id") String id);


    @Modifying
    @Query("UPDATE RoomTypeImage h SET h.isDeleted = true WHERE h.id = :ids and h.isDeleted = false")
    int deleteRoomTypeImageByIds(@Param("ids") List<String> ids);


    @Query("SELECT r FROM RoomTypeImage r WHERE r.roomTypeId = :roomTypeId AND r.isDeleted = false")
    List<RoomTypeImage> getRoomTypeImagesByRoomTypeId(@Param("roomTypeId") String roomTypeId);

    List<RoomTypeImage> getRoomTypeImagesByRoomTypeIdAndImageByteNotNull(String roomTypeId);
}
