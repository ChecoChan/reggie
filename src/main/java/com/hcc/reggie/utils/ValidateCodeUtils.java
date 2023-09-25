package com.hcc.reggie.utils;

import java.util.Random;

/** 随机生成验证码工具类 */
public class ValidateCodeUtils {
    /**
     * 随机生成验证码
     * @param length 生成验证码的长度
     */
    public static Integer generateValidateCode(int length) {
        Integer code = null;
        if (length == 4) {
            code = new Random().nextInt(9999); //生成随机数，最大为 9999
            if (code < 1000)
                code = code + 1000; // 保证随机数为 4 位数字
        } else if (length == 6) {
            code = new Random().nextInt(999999); //生成随机数，最大为 999999
            if (code < 100000)
                code = code + 100000; // 保证随机数为 6 位数字
        } else {
            throw new RuntimeException("只能生成 4 位或 6 位数字验证码");
        }
        return code;
    }
}
