package com.zhibiao.platform.shared.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 服务端生成的主键与短标识。业务主键在 JSON 中以字符串返回。
 */
public final class Ids {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUM = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private Ids() {
    }

    /** 32 位十六进制 UUID（无连字符） */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** 短随机标识，用于消息/事件/追踪 */
    public static String shortId() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(ALPHANUM[RANDOM.nextInt(ALPHANUM.length)]);
        }
        return sb.toString();
    }
}
