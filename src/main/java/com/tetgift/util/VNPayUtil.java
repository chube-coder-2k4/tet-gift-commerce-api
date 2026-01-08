package com.tetgift.util;

import com.tetgift.configuration.VNPayConfig;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class VNPayUtil {

    public static String buildPaymentUrl(Long paymentId, double amount, VNPayConfig config) {
        long vnpAmount = (long) (amount * 100);

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", config.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", paymentId.toString());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + paymentId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", config.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        String queryUrl = hashAllFields(vnp_Params);
        String vnp_SecureHash = hmacSHA512(config.getHashSecret(), queryUrl);

        log.info("=== VNPay SIGNATURE DEBUG (FINAL) ===");
        log.info("Hash Data (Query URL): {}", queryUrl);
        log.info("Secure Hash (SHA512): {}", vnp_SecureHash);
        log.info("====================================");

        return config.getUrl() + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    public static String hmacSHA512(String key, String data) {
        try {
            final byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA512");
            hmac.init(keySpec);
            byte[] macData = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // Convert to lowercase hex
            StringBuilder sb = new StringBuilder(2 * macData.length);
            for(byte b: macData) {
                sb.append(String.format("%02x", b&0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha512", e);
        }
    }

    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (!fieldValue.isEmpty()) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }
}
