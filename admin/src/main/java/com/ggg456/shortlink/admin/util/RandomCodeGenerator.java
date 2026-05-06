package com.ggg456.shortlink.admin.util;

import java.security.SecureRandom;

/**
 * 随机字符串生成工具类
 * 用于生成包含数字和字母（大小写）的指定位数随机字符串
 */
public final class RandomCodeGenerator {

    // 候选字符集：数字 + 小写字母 + 大写字母
    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    // 默认生成6位随机字符串
    private static final int DEFAULT_LENGTH = 6;

    // 工具类禁止实例化
    private RandomCodeGenerator() {
    }

    /**
     * 生成一个6位随机字符串（包含数字和字母）
     *
     * @return 6位随机字符串
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的随机字符串（包含数字和字母）
     *
     * @param length 字符串长度
     * @return 随机字符串
     */
    public static String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARS.length());
            sb.append(CHARS.charAt(index));
        }
        return sb.toString();
    }
}