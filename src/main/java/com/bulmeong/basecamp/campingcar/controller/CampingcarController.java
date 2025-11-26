package com.bulmeong.basecamp.campingcar.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.bulmeong.basecamp.campingcar.dto.BasicFacilitiesDto;
import com.bulmeong.basecamp.campingcar.dto.DriverAgeCondDto;
import com.bulmeong.basecamp.campingcar.dto.DriverExperienceCondDto;
import com.bulmeong.basecamp.campingcar.dto.DriverLicenseDto;
import com.bulmeong.basecamp.campingcar.dto.ProductDetailImgDto;
import com.bulmeong.basecamp.campingcar.dto.RentUserDto;
import com.bulmeong.basecamp.campingcar.dto.RentalExternalInspectionDto;
import com.bulmeong.basecamp.campingcar.dto.RentalReview;
import com.bulmeong.basecamp.campingcar.dto.ReservationDto;
import com.bulmeong.basecamp.campingcar.service.CampingcarService;
import com.bulmeong.basecamp.campingcar.service.PartnerCampingCarService;
import com.bulmeong.basecamp.common.util.ImageUtil;
import com.bulmeong.basecamp.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("campingcar")
public class CampingcarController {

    
    @Autowired
    private CampingcarService campingcarService;

    @Autowired
    private PartnerCampingCarService partnerCampingCarService;

    @RequestMapping("main")
    public String main(){

        return "campingcar/main";

    }

    @RequestMapping("gotoRentCar")
    public String gotoRentCar () {

        return "campingcar/gotoRentCar";
    }

@RequestMapping("campingCarDetailPage")
public String campingCarDetailPage(@RequestParam("id") int id, Model model, HttpSession session) {

    UserDto sessionUserInfo = (UserDto) session.getAttribute("sessionUserInfo");

    // sessionUserInfo가 null이면 로그인 페이지로 리다이렉트
    if (sessionUserInfo == null) {
        return "redirect:https://basecamp.bcyeon.click/user/login";  // 로그인 페이지로 리다이렉트
    }

    // 유저가 로그인한 경우 rentUserPk 조회 (null일 수 있음)
    Integer rentUserPk = campingcarService.getExistingByRentUserId(sessionUserInfo.getId());
    model.addAttribute("sessionUserInfo", sessionUserInfo);
    System.out.println("유저" + sessionUserInfo);

    // 캠핑카 상세 정보 가져오기
    Map<String, Object> campingcarDetails = campingcarService.getCampingCarDetailByid(id);
    model.addAttribute("campingcarDetails", campingcarDetails);
    System.out.println("디테일" + campingcarDetails);

    // 캠핑 옵션
    List<ProductDetailImgDto> detailImgDto = campingcarService.getProductDetailImgByProductId(id);
    model.addAttribute("detailImgDto", detailImgDto);
    System.out.println("캠핑옵션" + detailImgDto);

    // 좋아요 유무 확인 (rentUserPk가 null이어도 페이지 접근 가능)
    if (rentUserPk != null) {
        List<Map<String, Object>> MyLikeList = campingcarService.getMyLikeList(rentUserPk);
        model.addAttribute("MyLikeList", MyLikeList);
        System.out.println(MyLikeList);
    } else {
        model.addAttribute("MyLikeList", new ArrayList<>()); // 빈 리스트 설정
    }

    // 차량 옵션
    List<BasicFacilitiesDto> facilities = campingcarService.getBasicFacilitiesByProductId(id);
    model.addAttribute("facilities", facilities);
    System.out.println("차량옵션" + facilities);

    // 리뷰 리스트
    List<Map<String, Object>> reviewData = campingcarService.getReviewAllbyCarId(id);
    model.addAttribute("reviewData", reviewData);
    System.out.println("reviewData" + reviewData);

    // 해당 차량의 리뷰 별점 평균
    Double reviewAvg = campingcarService.getAvgByCarId(id);
    model.addAttribute("reviewAvg", reviewAvg);

    // 해당 차량의 리뷰 참여 인원 수
    int reivewCountBycar = campingcarService.getReviewByCountPersont(id);
    model.addAttribute("reivewCountBycar", reivewCountBycar);

    // 해당 차량의 각 별점마다 인원수
    List<Map<String, Object>> ratings = campingcarService.ratingGroupBycar(id);
    model.addAttribute("ratings", ratings);
    System.out.println("별점인원수" + ratings);

    return "campingcar/campingCarDetailPage";
}

    @RequestMapping("reservationInfo")
    public String reservationInfo(@RequestParam("id") int id, HttpSession session, Model model){

        UserDto sessionUserInfo =(UserDto)session.getAttribute("sessionUserInfo");
        // 사용자가 세션에 있는지 확인 
        if(sessionUserInfo != null) {
            boolean isRentUser = campingcarService.isRentUser(sessionUserInfo.getId());
            model.addAttribute("isRentUser", isRentUser);
        } else {
            model.addAttribute("isRentUser", false);
        }

        Map<String,Object> campingcarDetails = campingcarService.getCampingCarDetailByid(id);
        model.addAttribute("campingcarDetails", campingcarDetails);
        
        List<BasicFacilitiesDto> facilities = campingcarService.getBasicFacilitiesByProductId(id);
        model.addAttribute("facilities", facilities);

        // 차량등록_운전자 나이 Category List
        List<DriverAgeCondDto> driverAge = partnerCampingCarService.getDriverAgeAll(); 
        model.addAttribute("driverAge", driverAge);

        // 차량등록_운전 면허증 Category List
        List<DriverLicenseDto> driverLicense = partnerCampingCarService.getDriverLicenseAll();
        model.addAttribute("driverLicense", driverLicense);

        // 차량등록_운전자 경력 Category List
        List<DriverExperienceCondDto> driverExpericnece = partnerCampingCarService.getDriverExperienceAll();
        model.addAttribute("driverExpericnece", driverExpericnece);

        return "campingcar/reservationInfo";
    }

    // 예약프로세스
    @RequestMapping("reservationProcess")
    public String rentUserInfoProcess(HttpSession session, RentUserDto rentUser,
                                     @RequestParam("driveImage")MultipartFile driveImage, ReservationDto reservationDto,
                                     Model model) {

        UserDto sessionUserInfo = (UserDto)session.getAttribute("sessionUserInfo");
        int userPk = sessionUserInfo.getId();
        rentUser.setUser_id(userPk);

        String basecamp_rentUser = ImageUtil.saveImageAndReturnLocation(driveImage);
        
        rentUser.setDriver_license_image(basecamp_rentUser);

        campingcarService.registeRentUser(rentUser,reservationDto);

        Map<String,Object> reservationConfirm = campingcarService.getReservationDetails(reservationDto.getRent_user_id(), reservationDto.getId());
        model.addAttribute("reservationConfirm", reservationConfirm);

        return "campingcar/reservationConfirmation";
    }

    // 렌트 고객 확인 프로세스
    @RequestMapping("existingRentUserReservationProcess")
    public String existingRentUserReservationProcess(HttpSession session, ReservationDto reservationDto, Model model) {

        UserDto sessionUserInfo = (UserDto)session.getAttribute("sessionUserInfo");
        int rentUserPk = campingcarService.getExistingByRentUserId(sessionUserInfo.getId());
        reservationDto.setRent_user_id(rentUserPk);
        System.out.println("렌트고객확인: "+ rentUserPk);

        campingcarService.existingRentUserReservation(reservationDto);

        Map<String,Object> reservationConfirm = campingcarService.getReservationDetails(reservationDto.getRent_user_id(), reservationDto.getId());
        model.addAttribute("reservationConfirm", reservationConfirm);

        return "campingcar/reservationConfirmation";
    }

    // 이용내역 페이지
    @RequestMapping("rentUseageHistory")
    public String rentUseageHistory(HttpSession session, Model model) { 

        UserDto sessionUserInfo = (UserDto)session.getAttribute("sessionUserInfo");
        int rentUserPk = campingcarService.getExistingByRentUserId(sessionUserInfo.getId());
        System.out.println("렌트 고객 : " + rentUserPk);
        
        List<Map<String,Object>> rentuserHistoryData = campingcarService.getUseageHistroyAllByRentUserId(rentUserPk);

        model.addAttribute("rentuserHistoryData", rentuserHistoryData);
        

        return "campingcar/rentUseageHistory";
    }

    // 차량 예약(상세페이지 포함) 페이지
    @RequestMapping("rentalStatus")
    public String rentalStatus(HttpSession session, Model model) {

        UserDto sessionUserInfo = (UserDto)session.getAttribute("sessionUserInfo");
        int rentUserPk = campingcarService.getExistingByRentUserId(sessionUserInfo.getId());
        System.out.println("렌트 고객 : " + rentUserPk);
        
        List<Map<String,Object>> rentuserHistoryData = campingcarService.getUseageHistroyAllByRentUserId(rentUserPk);

        for(Map<String,Object> reservation :rentuserHistoryData){
            int reservation_id = (int) reservation.get("id");    
            boolean isReviewWritten = campingcarService.isReviewWritten(reservation_id);
            
            reservation.put("isReviewWritten", isReviewWritten);
        };
        model.addAttribute("rentuserHistoryData", rentuserHistoryData);

        return "campingcar/rentalStatus";
    }


    // 예약상세보기페이지
    @RequestMapping("rentalCarCheck")
    public String rentalCarCheck(@RequestParam("id") int id, HttpSession session, Model model) {

        Map<String,Object> rentCarData = campingcarService.findRetanlCarCheckList(id);
        boolean isReviewWritten = campingcarService.isReviewWritten(id);

        rentCarData.put("isReviewWritten", isReviewWritten);
        model.addAttribute("rentCarData", rentCarData);

        return "campingcar/rentalCarCheck";
    }

    // 리뷰작성하기
    @RequestMapping("carReviewPage")
    public String carReviewPage(@RequestParam("id")int id, HttpSession session, Model model) {

        UserDto sessionUserInfo = (UserDto)session.getAttribute("sessionUserInfo");
        int rentUserPk = campingcarService.getExistingByRentUserId(sessionUserInfo.getId());

        Map<String,Object> reservationConfirm = campingcarService.getReservationDetails(rentUserPk, id);
        model.addAttribute("reservationConfirm", reservationConfirm);
;

        return "campingcar/carReviewPage";
    }
    // 리뷰작성Process
    @RequestMapping("carReviewProcess")
    public String carReviewProcess(RentalReview review) {

        campingcarService.registerReview(review);

        return "redirect:https://basecamp.bcyeon.click/campingcar/main";
    }

    // 차량확인 사진찍기 페이지
    @RequestMapping("carExteriorInteriorShoot")
    public String carExteriorInteriorShoot(@RequestParam("id") int id, Model model) {
        System.out.println("reservation_id확인" + id);

        model.addAttribute("reservationId", id);


        return "campingcar/carExteriorInteriorShoot";
    }

    // 대여 사진 찍기 프로세스 
    @RequestMapping("rentShootProcess")
    public String rentShootProcess( @RequestParam("reservation_id") int reservation_id,
                                    @RequestParam("front_view") MultipartFile frontView,
                                    @RequestParam("passenger_front_view") MultipartFile passengerFrontView,
                                    @RequestParam("passenger_rear_view") MultipartFile passengerRearView,
                                    @RequestParam("rear_view") MultipartFile rearView,
                                    @RequestParam("driver_rear_view") MultipartFile driverRearView,
                                    @RequestParam("driver_front_view") MultipartFile driverFrontView,
                                    Model model) {

        String frontViewImg = ImageUtil.saveImageAndReturnLocation(frontView);
        String passengerFrontViewImg = ImageUtil.saveImageAndReturnLocation(passengerFrontView);
        String passengerRearViewImg = ImageUtil.saveImageAndReturnLocation(passengerRearView);
        String rearViewImg = ImageUtil.saveImageAndReturnLocation(rearView);
        String driverRearViewImg = ImageUtil.saveImageAndReturnLocation(driverRearView);
        String driverFrontViewImg = ImageUtil.saveImageAndReturnLocation(driverFrontView);

        RentalExternalInspectionDto rentalExternalInspectionDto = new RentalExternalInspectionDto();
        rentalExternalInspectionDto.setReservation_id(reservation_id);
        rentalExternalInspectionDto.setFront_view(frontViewImg);
        rentalExternalInspectionDto.setPassenger_front_view(passengerFrontViewImg);
        rentalExternalInspectionDto.setPassenger_rear_view(passengerRearViewImg);
        rentalExternalInspectionDto.setRear_view(rearViewImg); 
        rentalExternalInspectionDto.setDriver_rear_view(driverRearViewImg);
        rentalExternalInspectionDto.setDriver_front_view(driverFrontViewImg);

        campingcarService.registerRentShoot(rentalExternalInspectionDto);
        System.out.println("외관촬영 등록 확인 : " + rentalExternalInspectionDto);

        model.addAttribute("message", "파일이 성공적으로 업로드되었습니다.");
    return "redirect:https://basecamp.bcyeon.click/campingcar/rentalCarCheck?id="+ reservation_id;
    }

    // 차량 대여 사진찍기를 위한 이미지 메소드
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

    // 차량 반납 요청 페이지
    @RequestMapping("returnRequest")
    public String returnRequest (@RequestParam("id") int id, HttpSession session, Model model) {
        Map<String,Object> rentCarData = campingcarService.findRetanlCarCheckList(id);
        model.addAttribute("rentCarData", rentCarData);
        
        return "campingcar/returnRequest";
    }

    @RequestMapping("reservation_approved")
    public String reservation_approved(ReservationDto reservationDto) {
        System.out.println("reservation 확인" +reservationDto.getProgress());
    
        campingcarService.reservationApproved(reservationDto);
        System.out.println("id확인 " + reservationDto.getId());
    
        return "redirect:https://basecamp.bcyeon.click/campingcar/rentalCarCheck?id="+reservationDto.getId();
    }

    // 차량 반납 점검 동의 페이지
    @RequestMapping("returnInspectionAgreement")
    public String returnInspectionAgreement(@RequestParam("id")int id, Model model) {

        Map<String,Object> returnData = campingcarService.getReturnInspectionImgList(id);
        model.addAttribute("returnData", returnData);

        return "campingcar/returnInspectionAgreement";
    }






    // 좋아요 페이지
    @RequestMapping("myLike")
    public String myLike(HttpSession session,Model model){
        UserDto sessionUserInfo = (UserDto)session.getAttribute("sessionUserInfo");
        int rentUserPk = campingcarService.getExistingByRentUserId(sessionUserInfo.getId());
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@"+ rentUserPk);
        
        List<Map<String,Object>> MyLikeList = campingcarService.getMyLikeList(rentUserPk);
        System.out.println(MyLikeList);
        model.addAttribute("MyLikeList", MyLikeList);

        return "campingcar/myLike";
    }

    // 검색 기능 페이지
    @RequestMapping("searchResultsPage")
    public String searchResultsPage(
        @RequestParam(name = "location", required = false) String location,
        @RequestParam(name = "carTypes", required = false) List<String> carTypes,
        @RequestParam(name = "rentDate", required = false) String rentDate,
        @RequestParam(name = "returnDate", required = false) String returnDate,
        Model model) {

        Map<String, Object> map = new HashMap<>();
        map.put("location", location);
        map.put("carTypes", carTypes);

        if (rentDate != null && returnDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate rentDateTime = LocalDate.parse(rentDate, formatter);
            LocalDate returnDateTime = LocalDate.parse(returnDate, formatter);

            map.put("rentDate", java.sql.Date.valueOf(rentDateTime));
            map.put("returnDate", java.sql.Date.valueOf(returnDateTime));
        }

        List<Map<String, Object>> searchResultList = campingcarService.getSearchResultList(map);
        model.addAttribute("searchResultList", searchResultList);

        return "campingcar/searchResultsPage";
    }


}
