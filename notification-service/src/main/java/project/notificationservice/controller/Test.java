package project.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.commondto.dto.uri.CommonUri;
import project.notificationservice.client.UserClient;
import project.notificationservice.service.MailService;

@RestController
@RequiredArgsConstructor
@RequestMapping(CommonUri.VERSION + CommonUri.PUBLIC + CommonUri.NOTIFICATIONS)
@Slf4j
public class Test {
    public final MailService mailService;
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<?> test(){
//        String s = mailService.buildEmailBookingSuccess("");
//        mailService.send("chuonggiang2209@gmail.com",s,"Xác nhận đặt phòng");
        return new ResponseEntity<>(userClient.getUserById("00767361-bc49-4688-8fb5-18ffda6b007b"), HttpStatus.OK);
    }
}
