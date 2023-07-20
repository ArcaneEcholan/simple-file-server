package fit.wenchao.http_file_server.utils;


import cn.hutool.core.date.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {

    public static String format(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

}
