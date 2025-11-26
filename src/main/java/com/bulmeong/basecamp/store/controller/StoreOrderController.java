package com.bulmeong.basecamp.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bulmeong.basecamp.seller.dto.ApproveResponse;
import com.bulmeong.basecamp.seller.dto.ReadyResponse;
import com.bulmeong.basecamp.seller.service.KakaoPayService;
import com.bulmeong.basecamp.store.dto.StoreOrderDto;
import com.bulmeong.basecamp.store.service.StoreService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/order")
public class StoreOrderController {
    
    @Autowired
    private KakaoPayService kakaoPayService;

    @Autowired
    private StoreService storeService;

    @PostMapping("/pay/ready")
    public @ResponseBody ReadyResponse payReady(@RequestBody StoreOrderDto StoreOrderDto, HttpSession session){
        ReadyResponse readyResponse = kakaoPayService.readyPayment(StoreOrderDto);
        
        session.setAttribute("storeOrderDto", StoreOrderDto);
        session.setAttribute("tid", readyResponse.getTid());

        return readyResponse;
    }

    @GetMapping("/pay/completed")
    public String payCompleted(@RequestParam("pg_token") String pgToken, HttpSession session) {
        System.out.println("되는걸까");

        String tid = (String)session.getAttribute("tid");
        System.out.println("결제승인 요청을 인증하는 토큰: " + pgToken);
        System.out.println("결제 고유번호: " + tid);

        StoreOrderDto storeOrderDto = (StoreOrderDto)session.getAttribute("storeOrderDto");
        int order_id = storeOrderDto.getId();

        if (storeOrderDto != null) {
            // 카카오 결제 요청하기
            ApproveResponse approveResponse = kakaoPayService.payApprove(tid, pgToken, storeOrderDto.getId(), storeOrderDto.getUser_id());

            System.out.println(approveResponse);

            storeService.orderProcess(storeOrderDto.getUser_id(), storeOrderDto);
            int[] pendingOrderCartProductIds = (int[])session.getAttribute("pendingOrderCartProductIds");
            if(pendingOrderCartProductIds!=null){
                storeService.deleteCartProductDataList(pendingOrderCartProductIds);
            }
            session.removeAttribute("storeOrderDto");
        }
        
        return "redirect:https://basecamp.bcyeon.click/store/orderComplete?id="+order_id;
    }
}
