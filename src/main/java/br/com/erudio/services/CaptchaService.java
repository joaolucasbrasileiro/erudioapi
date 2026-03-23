package br.com.erudio.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CaptchaService {

    private static final String SECRET = "";
    private static final String VERIFY_URL = "https://api.hcaptcha.com/siteverify";

    public boolean verify(String token) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", SECRET);
        params.add("response", token);

        ResponseEntity<Map> response = restTemplate.postForEntity(VERIFY_URL, params, Map.class);

        Map body = response.getBody();
        return body != null && Boolean.TRUE.equals(body.get("success"));
    }
}

