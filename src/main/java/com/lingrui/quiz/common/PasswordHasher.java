package com.lingrui.quiz.common;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * 密码摘要工具。
 *
 * 用 PBKDF2-HMAC-SHA256 + 随机盐 + 多轮迭代，全部是 JDK 自带的东西，不引第三方依赖。
 *
 * 存储格式：pbkdf2$迭代次数$salt(base64)$hash(base64)
 * 把参数写在字符串里的好处是以后调迭代次数时，老数据仍然能验，不用全量重置密码。
 *
 * 为什么不用单轮 SHA-256：那是"同一密码永远同一个摘要、彩虹表一秒撞开"的存法。
 * 为什么要加盐：两个人用同一个密码，库里也必须长得不一样。
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /** 把明文密码转成可入库的摘要串 */
    public static String hash(String raw) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] digest = pbkdf2(raw.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        return PREFIX + "$" + ITERATIONS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(digest);
    }

    /**
     * 校验明文密码是否匹配库里存的摘要。
     * 现在没有登录接口用不到，但留着——以后加登录就靠它，也方便写测试。
     */
    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        byte[] actual = pbkdf2(raw.toCharArray(), salt, iterations, expected.length * 8);
        return constantTimeEquals(expected, actual);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("当前 JDK 不支持 " + ALGORITHM, e);
        }
    }

    /** 逐字节比较，不让耗时有差别，避免被用来猜密码 */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
