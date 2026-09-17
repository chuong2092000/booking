package project.hotelservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.hotelservice.entity.RoomInventory;

import java.util.List;

@Repository
public interface RoomInventoryRepo extends JpaRepository<RoomInventory, String>, JpaSpecificationExecutor<RoomInventory> {
    @Modifying
    @Query("UPDATE RoomInventory h SET h.isDeleted = true WHERE h.roomTypeId in (:roomTypeIds) and h.isDeleted = false")
    void deleteRoomInventoryByRoomTypeIds(@Param("roomTypeIds") List<String> roomTypeIds);

    @Modifying
    @Query("UPDATE RoomInventory h SET h.isDeleted = true WHERE h.roomTypeId = :roomTypeId and h.isDeleted = false")
    void deleteRoomInventoryByRoomTypeId(@Param("roomTypeId") String roomTypeId);

    @Modifying
    @Query("UPDATE RoomInventory h SET h.isDeleted = true WHERE h.id = :id and h.isDeleted = false")
    int deleteRoomInventoryById(@Param("id") String id);

    @Modifying
    @Query("UPDATE RoomInventory h SET h.isDeleted = true WHERE h.id in (:ids) and h.isDeleted = false")
    int deleteRoomInventoryByIds(@Param("ids") List<String> ids);
}
