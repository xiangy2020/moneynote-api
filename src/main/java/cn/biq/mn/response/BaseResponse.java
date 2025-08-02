package cn.biq.mn.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BaseResponse {

    private boolean success = false;
    private String message;

    public BaseResponse(boolean success) {
        this.success = success;
    }
}
