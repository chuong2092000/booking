package project.hotelservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.hotelservice.entity.HotelImage;

import java.util.List;

@Repository
public interface HotelImageRepo extends JpaRepository<HotelImage, String>, JpaSpecificationExecutor<HotelImage> {

    List<HotelImage> getHotelImagesByHotelIdAndIsDeleted(String hotelId, boolean isDeleted);

    @Modifying
    @Query("UPDATE HotelImage h SET h.isDeleted = true WHERE h.hotelId = :hotelId and h.isDeleted = false")
    void deleteHotelImageByHotelId(@Param("hotelId") String hotelId);

    @Modifying
    @Query("UPDATE HotelImage h SET h.isDeleted = true WHERE h.id = :id and h.isDeleted = false")
    int deleteHotelImageById(@Param("id") String id);

    @Modifying
    @Query("UPDATE HotelImage h SET h.isDeleted = true WHERE h.id = (:ids) and h.isDeleted = false")
    int deleteHotelImageByIds(@Param("ids") List<String> ids);

    List<HotelImage> getHotelImagesByHotelIdAndImageByteNotNull(String hotelId);
}