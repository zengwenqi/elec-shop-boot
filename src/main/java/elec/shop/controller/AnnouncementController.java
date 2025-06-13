package elec.shop.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "公告管理")
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {
}
