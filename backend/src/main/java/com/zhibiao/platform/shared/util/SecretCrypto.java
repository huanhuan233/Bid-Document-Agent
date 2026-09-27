package com.zhibiao.platform.shared.util;

import com.zhibiao.platform.shared.exception.BizException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 敏感配置（如 Dify API Key）落库加密：AES-256-GCM。
 * 主密钥由环境注入（Base64，32 字节），不得硬编码；日志不得打印密钥。
 */
public final class SecretCrypto {
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKeySpec key;

    public SecretCrypto(String base64MasterKey) {
        if (base64MasterKey == null || base64MasterKey.isBlank()) {
            this.key = null;
            return;
        }
        byte[] raw = Base64.getDecoder().decode(base64MasterKey.trim());
        if (raw.length != 32) {
            throw new IllegalStateException("ZB_SECRET_MASTER_KEY 必须是 32 字节的 Base64 字符串");
        }
        this.key = new SecretKeySpec(raw, "AES");
    }

    public boolean isConfigured() {
        return key != null;
    }

    public byte[] encrypt(String plain) {
        if (key == null) {
            throw BizException.badRequest("未配置主密钥 ZB_SECRET_MASTER_KEY，无法加密存储密钥");
        }
        try {
            byte[] iv = new byte[IV_LEN];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] enc = cipher.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return ByteBuffer.allocate(IV_LEN + enc.length).put(iv).put(enc).array();
        } catch (Exception e) {
            throw new IllegalStateException("加密失败", e);
        }
    }

    public String decrypt(byte[] stored) {
        if (key == null) {
            throw BizException.badRequest("未配置主密钥 ZB_SECRET_MASTER_KEY，无法解密密钥");
        }
        try {
            ByteBuffer buf = ByteBuffer.wrap(stored);
            byte[] iv = new byte[IV_LEN];
            buf.get(iv);
            byte[] enc = new byte[buf.remaining()];
            buf.get(enc);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(enc), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("解密失败（主密钥不匹配？）", e);
        }
    }

    /** 配置读取时只返回掩码 */
    public static String mask(String secret) {
        if (secret == null || secret.isBlank()) {
            return "";
        }
        String s = secret.trim();
        if (s.length() <= 8) {
            return "****";
        }
        return s.substring(0, 4) + "****" + s.substring(s.length() - 4);
    }
}
