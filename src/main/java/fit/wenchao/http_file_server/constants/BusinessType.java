package fit.wenchao.http_file_server.constants;

public class BusinessType {


    public static final BusinessType TOO_CROWD = new BusinessType("too crowd");
    String value;

    public String getValue() {
        return value;
    }

    private BusinessType() {

    }

    private BusinessType(String value) {
        this.value = value;
    }

    public static final BusinessType UserOnline = new BusinessType(
            "UserOnline");
    public static final BusinessType GetOnlineUsersResponse =
            new BusinessType("GetOnlineUsersResponse");
    public static final BusinessType Exception = new BusinessType("Exception");
    public static final BusinessType SendText = new BusinessType("SendText");
    public static final BusinessType UserOffline = new BusinessType("UserOffline");


    public static final BusinessType NULL = new BusinessType("NULL");



    public static BusinessType fromString(String value) {
        BusinessType[] values = new BusinessType[]{UserOnline,
                GetOnlineUsersResponse, Exception, SendText, UserOffline};
        for (BusinessType businessType : values) {
            if(businessType.toString().equals(value)){
                return businessType;
            }
        }
        return NULL;
    }

    @Override
    public String toString() {
        return value;
    }
}
