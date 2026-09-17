package project.paymentservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import project.commonutils.BaseJsonHelper;

@Component
public class JsonHelper extends BaseJsonHelper {
    public JsonHelper(@Autowired ObjectMapper objectMapper) {
        super(objectMapper);
    }
}