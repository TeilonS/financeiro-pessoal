package com.teilon.financeiro.service;

import com.teilon.financeiro.model.ConfigApp;
import com.teilon.financeiro.repository.ConfigAppRepository;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Gerencia as chaves VAPID: gera uma vez, persiste no banco H2 e expõe
 * os métodos necessários para assinar JWTs de autorização Web Push.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VapidService {

    private static final String KEY_PUBLIC  = "vapid.public";
    private static final String KEY_PRIVATE = "vapid.private";
    private static final String SUBJECT     = "mailto:teilon@financeiro-pessoal";

    private final ConfigAppRepository configRepo;

    @Getter private ECPublicKey  publicKey;
    @Getter private ECPrivateKey privateKey;
    @Getter private String       publicKeyBase64;

    @PostConstruct
    public void init() throws Exception {
        String pubStored  = configRepo.findById(KEY_PUBLIC).map(ConfigApp::getValor).orElse(null);
        String privStored = configRepo.findById(KEY_PRIVATE).map(ConfigApp::getValor).orElse(null);

        if (pubStored == null || privStored == null) {
            gerarEPersistir();
        } else {
            carregarDoBanco(pubStored, privStored);
        }
    }

    /** Cria o JWT de autorização VAPID para uma origin específica. */
    public String buildVapidJwt(String origin) {
        return Jwts.builder()
                .audience().add(origin).and()
                .subject(SUBJECT)
                .expiration(new Date(System.currentTimeMillis() + 12L * 3600 * 1000))
                .signWith(privateKey)
                .compact();
    }

    // ------------------------------------------------------------------ private

    private void gerarEPersistir() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
        KeyPair kp = gen.generateKeyPair();

        publicKey  = (ECPublicKey)  kp.getPublic();
        privateKey = (ECPrivateKey) kp.getPrivate();

        String pub  = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getEncoded());
        String priv = Base64.getUrlEncoder().withoutPadding().encodeToString(privateKey.getEncoded());
        publicKeyBase64 = pub;

        configRepo.save(new ConfigApp(KEY_PUBLIC,  pub));
        configRepo.save(new ConfigApp(KEY_PRIVATE, priv));

        log.info("=== VAPID keys geradas e salvas no banco. Chave pública para o frontend: {} ===", pub);
    }

    private void carregarDoBanco(String pub, String priv) throws Exception {
        byte[] pubBytes  = Base64.getUrlDecoder().decode(pub);
        byte[] privBytes = Base64.getUrlDecoder().decode(priv);

        KeyFactory kf = KeyFactory.getInstance("EC");
        publicKey       = (ECPublicKey)  kf.generatePublic(new X509EncodedKeySpec(pubBytes));
        privateKey      = (ECPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
        publicKeyBase64 = pub;
    }
}
