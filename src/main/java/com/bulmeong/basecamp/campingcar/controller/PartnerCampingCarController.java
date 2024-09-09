package com.bulmeong.basecamp.campingcar.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.UUID;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.bulmeong.basecamp.campingcar.dto.BasicFacilitiesDto;
import com.bulmeong.basecamp.campingcar.dto.CampingcarDto;
import com.bulmeong.basecamp.campingcar.dto.CarTypeDto;
import com.bulmeong.basecamp.campingcar.dto.DriverAgeCondDto;
import com.bulmeong.basecamp.campingcar.dto.DriverExperienceCondDto;
import com.bulmeong.basecamp.campingcar.dto.DriverLicenseDto;
import com.bulmeong.basecamp.campingcar.dto.LocationDto;
import com.bulmeong.basecamp.campingcar.dto.RentalCompanyDto;
import com.bulmeong.basecamp.campingcar.dto.RentalExternalInspectionDto;
import com.bulmeong.basecamp.campingcar.dto.RentalPeakPriceDto;
import com.bulmeong.basecamp.campingcar.dto.RentalReview;
import com.bulmeong.basecamp.campingcar.dto.ReservationDto;
import com.bulmeong.basecamp.campingcar.dto.ReturnExternalInspectionDto;
import com.bulmeong.basecamp.campingcar.service.CampingcarService;
import com.bulmeong.basecamp.campingcar.service.PartnerCampingCarService;
import com.bulmeong.basecamp.common.util.ImageUtil;


import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("partner")
public class PartnerCampingCarController {

    @Autowired 
    PartnerCampingCarService partnerCampingCarService;
    
    @Autowired
    CampingcarService campingcarService;

// 판매자 회원가입 
    @RequestMapping("nfRegisterPage")
    public String nfRegisterPage(Model model) {
        // 회원가입_회사지역 cate List
        List<LocationDto> locationData = partnerCampingCarService.getLocationAll();
        model.addAttribute("locationData", locationData);
        
        return "partner/nfRegisterPage";
    }

    // 판매자 회원가입 등록
    @RequestMapping("sellerRegisterProcess")
    public String sellerRegisterProcess(RentalCompanyDto rentalCompanyDto, 
                                        @RequestParam("profile_image") MultipartFile comp_profile_image
                                        ) {
        
        rentalCompanyDto.setComp_profile_image(ImageUtil.saveImageAndReturnLocation(comp_profile_image));
        
        partnerCampingCarService.registerSeller(rentalCompanyDto);

        return "redirect:https://basecamp.null-pointer-exception.com/seller/login";
    }
    // 판매자 로그인 
    @RequestMapping("loginProcess")
    public String loginProcess() {

        return "redirect:https://basecamp.null-pointer-exception.com/partner/partnerDashboard";
    }
    // 판매자 로그아웃 
    @RequestMapping("logoutProcess")
    public String logoutProcess(HttpSession session) {
        session.invalidate();
        return "redirect:https://basecamp.null-pointer-exception.com/seller/login";
    }
    // 판매자페이지 main
    // @RequestMapping("main")
    // public String main(HttpSession session, Model model){

    //     RentalCompanyDto rentalCompanyDto = (RentalCompanyDto) session.getAttribute("sessionCaravanInfo");
    //     model.addAttribute("rentalCompanyDto", rentalCompanyDto);
        
    //     return "partner/main";
    
    // }

    @RequestMapping("partnerDashboard")
    public String partnerDashboard(HttpSession session, Model model) {

        RentalCompanyDto rentalCompanyDto = (RentalCompanyDto) session.getAttribute("sessionCaravanInfo");
        model.addAttribute("rentalCompanyDto", rentalCompanyDto);
        
        return "partner/partnerDashboard";
    }

    // admin_main에 sub_category_쓰는 방식
    @RequestMapping("carRegister")
    public String carRegister(Model model, HttpSession session) {

    // 차량등록_캠핑카 유형 Category List
        List<CarTypeDto> carType = partnerCampingCarService.getCarTypeAll();
        model.addAttribute("carType", carType);
        
    // 차량등록_운전자 나이 Category List
        List<DriverAgeCondDto> driverAge = partnerCampingCarService.getDriverAgeAll(); 
        model.addAttribute("driverAge", driverAge);

    // 차량등록_운전 면허증 Category List
        List<DriverLicenseDto> driverLicense = partnerCampingCarService.getDriverLicenseAll();
        model.addAttribute("driverLicense", driverLicense);

    // 차량등록_운전자 경력 Category List
        List<DriverExperienceCondDto> driverExpericnece = partnerCampingCarService.getDriverExperienceAll();
        model.addAttribute("driverExpericnece", driverExpericnece);
        
    // 캠핑카 기본 보유 시설 Category List
        List<BasicFacilitiesDto> basicFacilities = partnerCampingCarService.getBasicFacilitiesAll();
        model.addAttribute("basicFacilities", basicFacilities);
        
        return "partner/carRegister";
    }

    // 차량등록 insert 
    @RequestMapping("carRegisterProgress")
    public String carRegisterProgress(CampingcarDto campingcarDto,@RequestParam("main_image")MultipartFile main_image
                                     ,@RequestParam("detailedImg") MultipartFile[] detailedImg
                                     ,@RequestParam(value = "basicFacilites_id") List<Integer> basicFacilites_id
                                     ,RentalPeakPriceDto rentalPeakPriceDto) {
        // for(MultipartFile img : detailedImg) {
        // }
                                        
        campingcarDto.setMain_img(ImageUtil.saveImageAndReturnLocation(main_image));

        partnerCampingCarService.registerCamping(campingcarDto,basicFacilites_id,detailedImg,rentalPeakPriceDto);
        System.out.println("차량등록 : " +campingcarDto+basicFacilites_id+detailedImg+rentalPeakPriceDto);
        return "redirect:https://basecamp.null-pointer-exception.com/partner/carManagement";
    }
    
    @RequestMapping("carManagement") 
    public String carManagement(){

        return "partner/carManagement";
    }
    
    @RequestMapping("peakSeason")
    public String peakSeason() {

        return "partner/peakSeason";
    }

    @RequestMapping("bookReservation")
    public String bookReservation(HttpSession session, Model model) {
        RentalCompanyDto rentalCompanyDto = (RentalCompanyDto) session.getAttribute("sessionCaravanInfo");

        List<Map<String,Object>> bookReservationList = partnerCampingCarService.getBookReservationAll(rentalCompanyDto.getId());
        model.addAttribute("bookReservationList", bookReservationList);
        return "partner/bookReservation";
    }

    @RequestMapping("reservation_approved")
    public String reservation_approved(ReservationDto reservationDto) {
    
        partnerCampingCarService.updateReservationProgress(reservationDto);
    
        return "redirect::https://basecamp.null-pointer-exception.com/partner/bookReservation";
    }

    @RequestMapping("reviewManage")
    public String reviewManage(HttpSession session, Model model) {
        
        RentalCompanyDto rentalCompanyDto = (RentalCompanyDto) session.getAttribute("sessionCaravanInfo");
        int rentalPk = rentalCompanyDto.getId();

        List<Map<String,Object>> reviewCompany = partnerCampingCarService.reviewManagebyRentCompanyId(rentalPk);
        model.addAttribute("reviewCompany", reviewCompany);
        
        return "partner/reviewManage";
    }

    @RequestMapping("reviewRelyContentProcess")
    public String reviewRelyContentProcess(RentalReview params) {
        
        partnerCampingCarService.updateReviewReply(params);

        return "redirect:https://basecamp.null-pointer-exception.com/partner/reviewManage";
    }

    @RequestMapping("rentalManagement")
    public String rentalManagement(HttpSession session, Model model) {
        
        RentalCompanyDto rentalCompanyDto = (RentalCompanyDto) session.getAttribute("sessionCaravanInfo");
        int rentalPk = rentalCompanyDto.getId();

        List<Map<String,Object>> rentalManageMetnList = partnerCampingCarService.getRentalManagementList(rentalPk);
        model.addAttribute("rentalManageMetnList", rentalManageMetnList);
        System.out.println("list" + rentalManageMetnList);

        return "partner/rentalManagement";
    }

    @RequestMapping("rentalShootDetailPage")
    public String rentalShootDetailPage(@RequestParam("id")int id, Model model) {
        
        List<Map<String,Object>> rentalShootList= partnerCampingCarService.getrentalShootList(id);
        model.addAttribute("rentalShootList", rentalShootList);
        System.out.println("retalShoot" + rentalShootList);

        return "partner/rentalShootDetailPage";
    }

    @RequestMapping("returnManagement")
    public String returnManagement(HttpSession session, Model model) {

        RentalCompanyDto rentalCompanyDto = (RentalCompanyDto) session.getAttribute("sessionCaravanInfo");
        int rentalPk = rentalCompanyDto.getId();

        List<Map<String,Object>> returnManageList= partnerCampingCarService.returnManagementList(rentalPk);
        model.addAttribute("returnManageList", returnManageList);
        
        return "partner/returnManagement";
    }

    @RequestMapping("returnImageUploadDetailPage")
    public String returnImageUploadDetailPage(@RequestParam("id") int id, Model model) {

        model.addAttribute("reservationId", id);
        
        return "partner/returnImageUploadDetailPage";
    }

    // 차량 반납 이미지 업로드 프로세스
    @RequestMapping("returnImageProcess")
    public String returnImageProcess(@RequestParam("reservation_id") int reservation_id,
                                    @RequestParam("front_view") MultipartFile frontView,
                                    @RequestParam("passenger_front_view") MultipartFile passengerFrontView,
                                    @RequestParam("passenger_rear_view") MultipartFile passengerRearView,
                                    @RequestParam("rear_view") MultipartFile rearView,
                                    @RequestParam("driver_rear_view") MultipartFile driverRearView,
                                    @RequestParam("driver_front_view") MultipartFile driverFrontView,
                                    Model model) {

        String frontViewImg = rentalShoot(frontView);
        String passengerFrontViewImg = rentalShoot(passengerFrontView);
        String passengerRearViewImg = rentalShoot(passengerRearView);
        String rearViewImg = rentalShoot(rearView);
        String driverRearViewImg = rentalShoot(driverRearView);
        String driverFrontViewImg = rentalShoot(driverFrontView);

        ReturnExternalInspectionDto returnExternalInspectionDto = new ReturnExternalInspectionDto();
        returnExternalInspectionDto.setReservation_id(reservation_id);
        returnExternalInspectionDto.setFront_view(frontViewImg);
        returnExternalInspectionDto.setPassenger_front_view(passengerFrontViewImg);
        returnExternalInspectionDto.setPassenger_rear_view(passengerRearViewImg);
        returnExternalInspectionDto.setRear_view(rearViewImg); 
        returnExternalInspectionDto.setDriver_rear_view(driverRearViewImg);
        returnExternalInspectionDto.setDriver_front_view(driverFrontViewImg);

        partnerCampingCarService.registerReturnShoot(returnExternalInspectionDto);
        System.out.println("반납 이미지 업로드 확인 "+ returnExternalInspectionDto);

        model.addAttribute("message", "파일이 성공적으로 업로드되었습니다.");
    return "redirect:https://basecamp.null-pointer-exception.com/partner/returnManagement";
    }

    // 차량 반납 이미지 업로드를 위한 이미지 메소드
    public String rentalShoot(MultipartFile newImage) {
        String rootPath = "C:/basecampImage/basecampeImage_rentuser/";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        String todayPath = sdf.format(new Date());

        File todayFolderForCreate = new File(rootPath + todayPath);
        if(!todayFolderForCreate.exists()) {
            todayFolderForCreate.mkdirs();
        }

        String originalFilename = newImage.getOriginalFilename();

        String uuid = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();

        String filename = uuid + "_" + currentTime;
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));

        filename += ext;

        try {
            newImage.transferTo(new File(rootPath + todayPath + filename));
        }catch(Exception e) {
            e.printStackTrace();
        }
        String newName = todayPath + filename;
        return newName;
    }


}


