package com.bulmeong.basecamp.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bulmeong.basecamp.store.dto.StoreBankAccountDto;
import com.bulmeong.basecamp.store.dto.StoreDeliveryInfoDto;
import com.bulmeong.basecamp.store.dto.StoreDto;
import com.bulmeong.basecamp.store.service.StoreService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("storeCenter")
public class StoreCenterController {

    @Autowired
    private StoreService storeService;

    @RequestMapping("logoutProcess")
    public String logoutProcess(HttpSession session){
        session.invalidate();

        return "redirect:https://basecamp.bcyeon.click/seller/login";
    }

    @RequestMapping("storeRegister")
    public String storeRegister(){
        return "store/pStoreRegisterPage";
    }

    @RequestMapping("storeRegisterProcess")
    public String storeRegisterProcess(StoreDto storeDto, StoreDeliveryInfoDto storeDeliveryInfoDto, StoreBankAccountDto storeBankAccountDto){

        storeService.registerStore(storeDto, storeDeliveryInfoDto, storeBankAccountDto);

        return "redirect:https://basecamp.bcyeon.click/seller/registerComplete";
    }

    @RequestMapping("dashboard")
    public String dashboard(){
        return "store/pDashboard";
    }

    @RequestMapping("productRegister")
    public String productRegister(Model model){
        model.addAttribute("productCategoryDtoList", storeService.getProductCategoryAll());
        return "store/pProductRegister";
    }

    @RequestMapping("storeInfo")
    public String storeInfo(){
        return "store/pStoreInfo";
    }
    
    @RequestMapping("sellerInfo")
    public String sellerInfo(HttpSession session, Model model){

        StoreDto sessionStore = (StoreDto)session.getAttribute("sessionStoreInfo");

        if(sessionStore!=null){
            StoreBankAccountDto storeBankAccountDto = storeService.getStoreBankAccountDtoByStoreId(sessionStore.getId());
            model.addAttribute("storeBankAccountDto", storeBankAccountDto);

            return "store/pSellerInfo";
        }else{
            return "redirect:https://basecamp.bcyeon.click/seller/login";
        }
        
    }

    @RequestMapping("deliveryInfo")
    public String deliveryInfo(){
        return "store/pDeliveryInfo";
    }

    @RequestMapping("sendProcessing")
    public String sendProcessing(){

        return "store/pSendProcessing";
    }

    @RequestMapping("orderProductDetails")
    public String orderDetails(@RequestParam("id") int id, Model model, HttpSession session){

        StoreDto storeDto = (StoreDto)session.getAttribute("sessionStoreInfo");
        int store_id = storeService.getStoreIdByStoreOrderId(id);

        if(storeDto==null||storeDto.getId() != store_id){
            return "redirect:https://basecamp.bcyeon.click/store"; //여기 먼가 잘못된 접근입니다 머 그런 느낌 페이지
        }
        model.addAttribute("orderProductData", storeService.getOrderProductData(id));

        return "store/orderProductDetails(Pop-up)";
    }

    @RequestMapping("productManage")
    public String productManage(Model model){
        
        model.addAttribute("productCategoryDtoList", storeService.getProductCategoryAll());

        return "store/pProductManage";
    }
    
    @RequestMapping("manageReview")
    public String manageReview(Model model){

        model.addAttribute("productCategoryDtoList", storeService.getProductCategoryAll());

        return "store/pManageReview";
    }

    @RequestMapping("orderIntegration")
    public String orderIntegration(){
        return "store/pOrderIntegration";
    }
    //////////////////////////////////////////////////////////////////////////

    @RequestMapping("cancelManage")
    public String cancelManage(){
        return "store/XcancelManage";
    }

    @RequestMapping("returnManage")
    public String returnManage(){
        return "store/XreturnManage";
    }

    @RequestMapping("manageQnA")
    public String manageQnA(){
        return "store/XmanageQnA";
    }

    @RequestMapping("balanceAccountsList")
    public String balanceAccountsList(){
        return "store/XbalanceAccountsList";
    }

    @RequestMapping("statistics")
    public String statistics(){
        return "store/Xstatistics";
    }

}
