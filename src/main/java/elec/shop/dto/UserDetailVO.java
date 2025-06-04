package elec.shop.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserDetailVO {
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String email;
    private String mobile;
    private Integer gender;
    private Date birthDate;
    private Integer status;
    private Integer userType;
    private Date lastLoginTime;
    private String lastLoginIp;
    private List<String> roleNames;
    private List<String> permissions;
} 