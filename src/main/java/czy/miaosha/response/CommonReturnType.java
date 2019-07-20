package czy.miaosha.response;

public class CommonReturnType {

    //返回success或fail
    private String status;

    //success:返回前端需要的数据
    //fail:返回自定义的错误码
    private Object data;

    public static CommonReturnType create(Object result) {
        return CommonReturnType.create(result, "success");
    }

    public static CommonReturnType create(Object result, String status) {
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data)  {
        this.data = data;
    }
}
