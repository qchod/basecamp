package com.bulmeong.basecamp.seller.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bulmeong.basecamp.camp.dto.CampsiteOrderDto;
import com.bulmeong.basecamp.camp.mapper.CampsiteSqlMapper;
import com.bulmeong.basecamp.seller.dto.ApproveResponse;
import com.bulmeong.basecamp.seller.dto.ReadyResponse;
import com.bulmeong.basecamp.store.dto.StoreOrderDto;

import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoPayService {
    @Value("${api.key}")
    private String apiKey;

    @Autowired
    private CampsiteSqlMapper campsiteMapper;

    public ReadyResponse readyPayment(StoreOrderDto storeOrderDto){

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization","SECRET_KEY " + apiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cid", "TC0ONETIME");
        requestBody.put("partner_order_id", storeOrderDto.getId());
        requestBody.put("partner_user_id", storeOrderDto.getUser_id());
        requestBody.put("item_name", "BASECAMP STORE");
        requestBody.put("quantity", "1");
        requestBody.put("total_amount", storeOrderDto.getPayment_amount());
        requestBody.put("tax_free_amount", "0");
        requestBody.put("approval_url", "https://basecamp.bcyeon.click/order/pay/completed");
        requestBody.put("fail_url", "https://basecamp.bcyeon.click/store");
        requestBody.put("cancel_url", "https://basecamp.bcyeon.click/store");

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";

        ResponseEntity<ReadyResponse> responseEntity = restTemplate.postForEntity(url, requestEntity, ReadyResponse.class);
        System.out.println("결제준비 응답 객체: " + responseEntity.getBody());

        return responseEntity.getBody();
    }

    public ReadyResponse readyPayment(CampsiteOrderDto orderDto){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization","SECRET_KEY " + apiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("cid", "TC0ONETIME");
        requestBody.put("partner_order_id", "캠핑장예약");
        requestBody.put("partner_user_id", orderDto.getCustomer_name());
        System.out.println(campsiteMapper.pointById(orderDto.getPoint_id()));
        requestBody.put("item_name", campsiteMapper.pointById(orderDto.getPoint_id()).getName());
        requestBody.put("quantity", "1");
        requestBody.put("total_amount", orderDto.getTotal_prise());
        requestBody.put("tax_free_amount", "0");
        requestBody.put("approval_url", "https://basecamp.bcyeon.click/camp/pay/completed");
        requestBody.put("fail_url", "https://basecamp.bcyeon.click/camp");
        requestBody.put("cancel_url", "https://basecamp.bcyeon.click/camp");

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";

        ResponseEntity<ReadyResponse> responseEntity = restTemplate.postForEntity(url, requestEntity, ReadyResponse.class);
        System.out.println("결제준비 응답 객체: " + responseEntity.getBody());

        return responseEntity.getBody();
    }

    public ApproveResponse payApprove(String tid, String pgToken, int order_id, int user_id) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + apiKey);
        headers.set("Content-Type", "application/json");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cid", "TC0ONETIME");
        parameters.put("tid", tid);
        parameters.put("partner_order_id", order_id);
        parameters.put("partner_user_id", user_id);
        parameters.put("pg_token", pgToken);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(parameters, headers);

        RestTemplate template = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";
        ApproveResponse approveResponse = template.postForObject(url, requestEntity, ApproveResponse.class);

        System.out.println(approveResponse);

        return approveResponse;
    }
}

