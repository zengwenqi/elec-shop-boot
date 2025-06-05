package elec.shop.pojo.sys.dto;

import lombok.Data;

import java.util.List;

@Data
public class MenuVO {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer sortOrder;
    private Boolean hidden;
    private Boolean keepAlive;
    private List<MenuVO> children;
}
