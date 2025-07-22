package elec.shop.pojo.purchase.enums;

public enum PurchaseOrderDraftErrorCode {
    DRAFT_NOT_EXIST(70001, "暂存数据不存在"),
    DRAFT_EXPIRED(70002, "暂存数据已过期"),
    DRAFT_SAVE_FAILED(70003, "暂存数据保存失败"),
    DRAFT_FORMAT_ERROR(70004, "暂存数据格式错误");
    
    private final int code;
    private final String message;
    
    PurchaseOrderDraftErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
