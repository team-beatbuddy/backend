package com.ceos.beatbuddy.global.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

public class AppleClientSecretUtil {

    public static String generateClientSecretFromString(
            String teamId,
            String clientId,
            String keyId,
            String privateKeyString
    ) {
        try {
            String privateKeyContent = privateKeyString
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] pkcs8Bytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Bytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(keySpec);

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(keyId)
                    .type(JOSEObjectType.JWT)
                    .build();

            long now = System.currentTimeMillis();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(teamId)
                    .issueTime(new Date(now))
                    .expirationTime(new Date(now + 86400L * 1000 * 180)) // 180일
                    .audience("https://appleid.apple.com")
                    .subject(clientId)
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            JWSSigner signer = new ECDSASigner(privateKey);
            jwt.sign(signer);

            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate Apple client secret", e);
        }
    }
}