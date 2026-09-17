package project.hotelservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.commondto.dto.hotel.SearchHotelDto;
import project.commondto.dto.uri.CommonUri;
import project.commonutils.BaseUtils;
import project.hotelservice.service.hotel.PublicHotelService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.PUBLIC + CommonUri.HOTELS)
public class PublicHotelController {

    private final PublicHotelService publicHotelService;

    @GetMapping
    public ResponseEntity<?> getHotels(SearchHotelDto searchHotelDto, Pageable pageable) {
        return BaseUtils.dataResponse("Get hotels success",
                publicHotelService.getHotels(searchHotelDto, pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHotel(@PathVariable String id) {
        return BaseUtils.dataResponse("Get hotel success",
                publicHotelService.getHotel(id), HttpStatus.OK);
    }
}
