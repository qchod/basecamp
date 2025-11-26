package com.bulmeong.basecamp.store.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bulmeong.basecamp.store.dto.ProductWishDto;
import com.bulmeong.basecamp.store.dto.StoreOrderDto;
import com.bulmeong.basecamp.store.dto.UserDeliveryInfoDto;
import com.bulmeong.basecamp.store.service.StoreService;
import com.bulmeong.basecamp.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @RequestMapping("")
    public String main(Model model){
        model.addAttribute("allProductDataList", storeService.getAllProductDataList());
        model.addAttribute("bestProductDataList", storeService.getFiveProductDataList());

        return "store/mStoreMain";
    }

    @RequestMapping("category")
    public String category(@RequestParam("category_id") int category_id, Model model){
        
        Map<String, Object> productCategoryData = storeService.getProductCategoryDataByCategoryId(category_id);
        model.addAttribute("productCategoryData", productCategoryData);

        return "store/mCategory";
    }

    @RequestMapping("productDetails")
    public String productDetails(@RequestParam("id") int id, Model model, HttpSession session){

        Map<String, Object> productData = storeService.getProductDataByProductId(id);
        model.addAttribute("productData", productData);

        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        if(userDto == null){
            model.addAttribute("isWishlisted", false);
        }else{
            ProductWishDto productWishDto = new ProductWishDto();
            productWishDto.setProduct_id(id);
            productWishDto.setUser_id(userDto.getId());
            
            model.addAttribute("isWishlisted", storeService.isWishlisted(productWishDto));
        }
        
        return "store/mProductDetails";
    }

    @RequestMapping("cart")
    public String cart(){

        return "store/mCart";
    }

    @RequestMapping("ordersheet")
    public String ordersheet(HttpSession session, Model model){

        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        int user_id = userDto.getId();

        List<Map<String, Object>> pendingOrderProductInfoDataList = storeService.getPendingOrderDataList(user_id);
        model.addAttribute("pendingOrderProductInfoDataList", pendingOrderProductInfoDataList);

        UserDeliveryInfoDto userDeliveryInfoDto = storeService.selectDefaultAddressByUserId(user_id);
        model.addAttribute("userDeliveryInfoDto", userDeliveryInfoDto);

        int order_id = storeService.getPendingOrderId(user_id);
        model.addAttribute("order_id", order_id);

        return "store/mOrdersheet(new)";
    }

    @RequestMapping("orderComplete")
    public String orderComplete(@RequestParam("id") int id, HttpSession session, Model model){
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");

        StoreOrderDto storeOrderDto = storeService.getStoreOrderDtoById(id);
        
        if(userDto==null||storeOrderDto.getUser_id()!=userDto.getId()){
            return "redirect:https://basecamp.bcyeon.click/store";
        }else{
            model.addAttribute("orderData", storeService.getOrderDataByOrderId(id));
            return "store/mOrderComplete";
        }
    }

    // @RequestMapping("my")
    // public String my(HttpSession session){
    //     UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");

    //     if(userDto == null){
    //         return "redirect:https://basecamp.bcyeon.click/user/login";
    //     }else{
    //         return "store/mMy";
    //     }
    // }

    @RequestMapping("myOrderList")
    public String myOrderList(HttpSession session, Model model){
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");

        if(userDto==null){
            return "redirect:https://basecamp.bcyeon.click/user/login";
        }else{
            int user_id = userDto.getId();
            Map<String, Object> map = storeService.getOrderStatusCountData(user_id);
            model.addAttribute("orderStatusCountData", map);
        }

        return "store/mMyOrderList";
    }

    @RequestMapping("orderView")
    public String orderView(@RequestParam("id") int id, Model model){

        Map<String, Object> orderData = storeService.getStoreOrderDataListByOrderId(id);
        model.addAttribute("orderData", orderData);

        int refundPrice = storeService.getRefundPriceSum(id);
        model.addAttribute("refundPrice", refundPrice);

        return "store/mOrderView";
    }

    @RequestMapping("orderRefund")
    public String orderRefund(@RequestParam("id") int id, Model model){

        model.addAttribute("orderProductData", storeService.getOrderProductDataForRefund(id));
        model.addAttribute("refundReasonList", storeService.getRefundReasonList());

        return "store/mOrderRefund";
    }

    @RequestMapping("addressBook")
    public String addressBook(HttpSession session, Model model){

        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");

        if(userDto==null){
            return "redirect:https://basecamp.bcyeon.click/user/login";
        }else{
            List<UserDeliveryInfoDto> userDeliveryInfoDtoList = storeService.getUserDeliveryInfoByUserId(userDto.getId());
            model.addAttribute("userDeliveryInfoDtoList", userDeliveryInfoDtoList);

            return "store/mAddressBook";
        }
    }

    @RequestMapping("deliveryForm")
    public String deliveryForm(@RequestParam("prevPage") String prevPage){

        return "store/mDeliveryForm";
    }

    @RequestMapping("writeReview")
    public String writeReview(@RequestParam("id")int id, Model model){
        //이 유저의 주문상품이 아니면 접근 불가...

        model.addAttribute("orderProductData", storeService.getOrderProductDataForReview(id));
        
        return "store/mWriteReview";
    }

    @RequestMapping("reviewComplete")
    public String reviewComplete(){

        return "store/mReviewComplete";
    }

    @RequestMapping("myReview")
    public String myReview(HttpSession session, Model model){
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");

        model.addAttribute("orderStatusCountData", storeService.getOrderStatusCountData(userDto.getId()));

        return "store/mMyReview";
    }

    @RequestMapping("refundRequestComplete")
    public String refundRequestComplete(@RequestParam("id") int id, Model model, HttpSession session){
        
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");

        Map<String, Object> refundData = storeService.getRefundProductDataForCompletePage(id);

        if(userDto == null || userDto.getId()!= (int)refundData.get("user_id")){
            return "redirect:https://basecamp.bcyeon.click/store";
        }else{
            model.addAttribute("refundData", refundData);
            return "store/mRefundRequestComplete";
        }

    }

    // @RequestMapping("tempLogin")
    // public String tempLogin(){

    //     return "common/tempLogin";
    // }

    @RequestMapping("bestProduct")
    public String bestProduct(Model model){
        //여기 수정
        model.addAttribute("bestProductDataList", storeService.getTenProductDataList());
        model.addAttribute("productCategoryList", storeService.getProductCategoryAll());

        return "store/mBestProduct";
    }

    @RequestMapping("newProduct")
    public String newProduct(Model model){
        model.addAttribute("newProductDataList", storeService.getTenProductDataList());
        model.addAttribute("newStoreData", storeService.getNewStoreData());

        return "store/mNewProduct";
    }

    @RequestMapping("wish")
    public String wish(Model model, HttpSession session){
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        int user_id = userDto.getId();
        model.addAttribute("wishDataList", storeService.getWishListByUserId(user_id));
        model.addAttribute("productCategoryList", storeService.getProductCategoryAll());

        return "store/mWish";
    }

    @RequestMapping("brand")
    public String brand(){
        return "store/mBrand";
    }

    @RequestMapping("myClaimList")
    public String myClaimList(){
        return "store/mMyClaimList";
    }
}
