package project.hotelservice.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import project.commondto.dto.hotel.RoomInventoryUpdateAction;
import project.commondto.dto.hotel.dto.RoomInventoryDto;
import project.commondto.exception.BusinessException;
import project.commonutils.BaseUtils;
import project.hotelservice.entity.RoomInventory;
import project.hotelservice.repo.RoomInventoryRepo;
import project.hotelservice.spec.RoomInventorySpec;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoreRoomInventoryService {

    private final RoomInventoryRepo roomInventoryRepo;

    public List<RoomInventory> updateQuantityRoomInventory(List<RoomInventoryDto> roomInventoriesDto, RoomInventoryUpdateAction roomInventoryUpdateAction) {

        BaseUtils.validateListObject(roomInventoriesDto, "Room inventories DTO", false);

        List<String> ids = roomInventoriesDto.stream()
                .map(RoomInventoryDto::getId)
                .toList();

        List<RoomInventory> roomInventories = roomInventoryRepo.findAll(
                RoomInventorySpec.hasIds(ids).and(RoomInventorySpec.isDeleted(false)));

        BaseUtils.validateListObject(roomInventories, "Room inventories not found with ids: " + ids, true);

        if (roomInventories.size() != roomInventoriesDto.size()) {
            log.warn("Room inventory invalid with ids: {}", ids);
            throw new BusinessException("Room inventory invalid with ids; " + ids);
        }

        Map<String, RoomInventory> roomInventoryMap = roomInventories.stream()
                .collect(Collectors.toMap(RoomInventory::getId, inv -> inv));

        List<RoomInventory> toUpdate = new ArrayList<>();

        for (RoomInventoryDto roomInventoryDto : roomInventoriesDto) {
            RoomInventory roomInventory = roomInventoryMap.get(roomInventoryDto.getId());

            BaseUtils.validateObject(roomInventory, "Room inventory not found with id: " + roomInventoryDto.getId(), true);

            handleRoomTypeQuantity(roomInventory, roomInventoryDto.getAvailableQuantity(), roomInventoryUpdateAction);

            toUpdate.add(roomInventory);
        }

        return roomInventoryRepo.saveAll(toUpdate);
    }

    public List<RoomInventory> getRoomInventoriesByRoomTypeId(String roomTypeId, LocalDate fromDate, LocalDate toDate, Integer quantity) {
        BaseUtils.validateObject(roomTypeId, "Room type id", false);
        Specification<RoomInventory> roomInventorySpec = RoomInventorySpec.hasRoomTypeId(roomTypeId)
                .and(RoomInventorySpec.isDeleted(false))
                .and(RoomInventorySpec.isDateBetween(fromDate,toDate))
                .and(RoomInventorySpec.isQuantity(quantity));
        return roomInventoryRepo.findAll(roomInventorySpec);
    }
    public List<RoomInventory> getRoomInventoriesByRoomTypeIds(List<String> roomTypeIds, LocalDate fromDate, LocalDate toDate, Integer quantity) {
        BaseUtils.validateListObject(roomTypeIds, "Room type ids", false);
        Specification<RoomInventory> roomInventorySpec = RoomInventorySpec.hasRoomTypeIds(roomTypeIds)
                .and(RoomInventorySpec.isDeleted(false))
                .and(RoomInventorySpec.isDateBetween(fromDate,toDate))
                .and(RoomInventorySpec.isQuantity(quantity));
        return roomInventoryRepo.findAll(roomInventorySpec);
    }

    public void deleteRoomInventoryByRoomTypeIds(List<String> roomTypeIds) {
        BaseUtils.validateListObject(roomTypeIds, "Room type ids", false);
        roomInventoryRepo.deleteRoomInventoryByRoomTypeIds(roomTypeIds);
    }

    public void deleteRoomInventoryByRoomTypeId(String roomTypeId) {
        BaseUtils.validateObject(roomTypeId, "Room type id", false);
        roomInventoryRepo.deleteRoomInventoryByRoomTypeId(roomTypeId);
    }

    public void deleteRoomInventoryById(String id) {
        BaseUtils.validateObject(id, "Room inventory id", false);
        if (roomInventoryRepo.deleteRoomInventoryById(id) <= 0) {
            log.warn("Delete room inventory failed with id: {}", id);
            throw new BusinessException("Room inventory cannot delete with id: " + id);
        }
    }

    public void deleteRoomInventoryByIds(List<String> ids) {
        BaseUtils.validateListObject(ids, "Room inventory ids", false);
        if (roomInventoryRepo.deleteRoomInventoryByIds(ids) <= 0) {
            log.warn("Delete room inventory failed with ids: {}", ids);
            throw new BusinessException("Room inventory cannot delete with ids: " + ids);
        }
    }


    private void handleRoomTypeQuantity(RoomInventory roomInventory, int target, RoomInventoryUpdateAction roomInventoryUpdateAction) {
        if (target < 0) {
            throw new BusinessException("Target quantity must be positive!");
        }

        int availableQuantity = roomInventory.getAvailableQuantity();
        int reservedQuantity = roomInventory.getReservedQuantity();
        int totalQuantity = roomInventory.getTotalQuantity();

        switch (roomInventoryUpdateAction) {
            case HOLD -> {
                if (availableQuantity < target) {
                    throw new BusinessException("Not enough available rooms for id: " + roomInventory.getId());
                }
                availableQuantity -= target;
                reservedQuantity += target;
            }
            case REVERT -> {
                if (reservedQuantity < target) {
                    throw new BusinessException("Not enough reserved rooms to revert for id: " + roomInventory.getId());
                }
                availableQuantity += target;
                reservedQuantity -= target;
            }
        }

        if (availableQuantity + reservedQuantity != totalQuantity) {
            throw new BusinessException("Data integrity violation: Available + Reserved != Total quantity for ID: " + roomInventory.getId());
        }

        roomInventory.setAvailableQuantity(availableQuantity);
        roomInventory.setReservedQuantity(reservedQuantity);
    }
}
