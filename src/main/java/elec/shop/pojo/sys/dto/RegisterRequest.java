package elec.shop.pojo.sys.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String realName;
    private String email;
    private String mobile;
    private Integer gender;
    private String avatar;
    private String emailCode;
}
