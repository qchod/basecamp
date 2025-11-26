package com.bulmeong.basecamp.club.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;

import com.bulmeong.basecamp.club.dto.ClubBookmarkDto;
import com.bulmeong.basecamp.club.dto.ClubCategoryDto;
import com.bulmeong.basecamp.club.dto.ClubDto;
import com.bulmeong.basecamp.club.dto.ClubJoinConditionDto;
import com.bulmeong.basecamp.club.dto.ClubMeetingDto;
import com.bulmeong.basecamp.club.dto.ClubMeetingMemberDto;
import com.bulmeong.basecamp.club.dto.ClubMemberDto;
import com.bulmeong.basecamp.club.dto.ClubPostCategoryDto;
import com.bulmeong.basecamp.club.dto.ClubPostCommentDto;
import com.bulmeong.basecamp.club.dto.ClubPostDto;
import com.bulmeong.basecamp.club.dto.ClubPostImageDto;
import com.bulmeong.basecamp.club.dto.ClubPostLikeDto;
import com.bulmeong.basecamp.club.dto.ClubRegionCategoryDto;
import com.bulmeong.basecamp.club.dto.ClubVisitDto;
import com.bulmeong.basecamp.club.mapper.ClubSqlMapper;
import com.bulmeong.basecamp.club.service.ClubService;
import com.bulmeong.basecamp.common.dto.ImageDto;

import com.bulmeong.basecamp.common.util.ImageUtil;
import com.bulmeong.basecamp.common.util.Utils;
import com.bulmeong.basecamp.secondHandProduct.dto.CategoryDto;
import com.bulmeong.basecamp.user.dto.UserDto;
import com.bulmeong.basecamp.user.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("club")

public class ClubController {

    @Autowired
    private Utils util;

    @Autowired UserService userService;
    @Autowired ClubSqlMapper clubSqlMapper;

    @Autowired
    private ClubService clubService;

    @RequestMapping("main")
    public String clubMain(HttpSession session, Model model){
        

        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        model.addAttribute("userDto", userDto);

        List<ClubRegionCategoryDto> regionCategoryDtoList = clubService.findRegionCategory();
        model.addAttribute("regionCategoryDtoList", regionCategoryDtoList);

        if (userDto != null) {
            // 북마크한 소모임 목록 3개 리미트 걸기
            List<Map<String, Object>> bookmarkedClubDataList = clubService.getBookmarkedClubDtoList(userDto.getId());
            List<Map<String,Object>> limitedBookmarkedClubDtoList = bookmarkedClubDataList.stream()
            .limit(10)
            .collect(Collectors.toList());
            
            model.addAttribute("bookmarkedClubDataList", limitedBookmarkedClubDtoList);

            //  내가 가입한 소모임 목록
            List<ClubDto> joinClubDtoList = clubService.findJoinClubDtoList(userDto.getId());
            List<ClubDto> limitedJoinClubDtoList = joinClubDtoList.stream()
            .limit(10)
            .collect(Collectors.toList());
            model.addAttribute("joinClubDtoList", limitedJoinClubDtoList);
        }

            // 새로운 소모임 목록
            List<ClubDto> clubDtoList = clubService.findClubDtoList();
            List<ClubDto> limitedClubDtoList = clubDtoList.stream()
            .limit(10)
            .collect(Collectors.toList());
            model.addAttribute("clubDtoList", limitedClubDtoList);

            // 인기글
            List<Map<String,Object>> hotPostDataList = clubService.getHotPosts();
            model.addAttribute("hotPostDataList", hotPostDataList);

            return "club/clubMainPage";

       
    }

    @RequestMapping("home")
    public String clubHome(@RequestParam("id") int id, Model model, HttpSession session){
        model.addAttribute("id", id);
        Map<String, Object>map = clubService.clubDetail(id);
        model.addAttribute("map", map);

        // List<Map<String,Object>>  clubMeetingDataList =clubService.selectClubMeetingDtoList(id);
        // model.addAttribute("clubMeetingDataList", clubMeetingDataList);

        // int totalMeetings = clubService.countTotalMeeting(id);
        // model.addAttribute("totalMeetings", totalMeetings);

        ClubBookmarkDto clubBookmarkDto = new ClubBookmarkDto();
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        
        if(userDto != null){
            clubBookmarkDto.setUser_id(userDto.getId());
            clubBookmarkDto.setClub_id(id);
            int confirmBookmark = clubService.confirmBookmark(clubBookmarkDto);
            model.addAttribute("confirmBookmark", confirmBookmark);
        }

        int totalBookmark = clubService.countTotalBookmark(id);
        model.addAttribute("totalBookmark", totalBookmark);       


        List<Map<String, Object>> clubMemberDataList = clubService.findClubMemerDataList(id);
        model.addAttribute("clubMemberDataList", clubMemberDataList);
        System.out.println(clubMemberDataList);

        ClubMemberDto clubMemberDto = new ClubMemberDto();
        

        if(userDto != null){
            clubMemberDto.setClub_id(id);
            clubMemberDto.setUser_id(userDto.getId());
            int isMemberInClub = clubService.checkClubMembership(clubMemberDto);
            model.addAttribute("isMemberInClub", isMemberInClub);
        }

        int totalClubMember = clubService.countTotalClubMember(id);
        int confirmCapacity = clubService.confirmCapacity(id);
        int countTodayVisit = clubService.countTodayVisit(id);

        model.addAttribute("totalClubMember", totalClubMember);
        model.addAttribute("confirmCapacity", confirmCapacity);
        model.addAttribute("countTodayVisit", countTodayVisit);

        ClubVisitDto clubVisitDto = new ClubVisitDto();
        clubVisitDto.setClub_id(id);
        clubVisitDto.setUser_id(userDto.getId());

        clubService.increaseVisitCount(clubVisitDto);
        int visitCount = clubService.selectTodayVisitCount(id);
        model.addAttribute("visitCount", visitCount);

        return "club/clubHomePage";
    }

    @RequestMapping("createNewClub")
    public String createNewClub(Model model){

        List<ClubRegionCategoryDto> regionCategoryDtoList = clubService.findRegionCategory();
        model.addAttribute("regionCategoryDtoList", regionCategoryDtoList);

        List<ClubCategoryDto> clubCategoryDtoList = clubService.findClubCategory();
        model.addAttribute("clubCategoryDtoList", clubCategoryDtoList);
        
        return "club/createNewClubPage";
    }
    
    @RequestMapping("createNewClubProcess")
    public String createClubProcess(ClubDto clubDto, @RequestParam("main_img") MultipartFile main_img, ClubJoinConditionDto clubJoinConditionDto){
        clubDto.setMain_image(ImageUtil.saveImageAndReturnLocation(main_img));
        clubService.createNewClub(clubDto);
      

        ClubMemberDto clubMemberDto = new ClubMemberDto();
        clubMemberDto.setUser_id(clubDto.getUser_id());
        clubMemberDto.setRole_id(1);
        clubMemberDto.setClub_id(clubDto.getId());
        clubService.joinClub(clubMemberDto);

        ClubJoinConditionDto joinConditionDto = new ClubJoinConditionDto();
        joinConditionDto.setClub_id(clubDto.getId());
        joinConditionDto.setStart_year(clubJoinConditionDto.getStart_year());
        joinConditionDto.setEnd_year(clubJoinConditionDto.getEnd_year());
        joinConditionDto.setGender(clubJoinConditionDto.getGender());
        clubService.insertClubJoinCondition(joinConditionDto);

        return "redirect:https://basecamp.bcyeon.click/club/main";
    }

    // 소모임 회원가입
    @RequestMapping("joinClub")
    public String joinClub(@RequestParam("id") int id, Model model){
        model.addAttribute("id", id);
        return "club/joinClubPage";
    }

    @RequestMapping("joinClubProcess")
    public String joinClubProcess(@RequestParam("club_id") int id, Model model, HttpSession session){
        model.addAttribute("id", id);
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        ClubMemberDto clubMemberDtoForRoleId3 = new ClubMemberDto();
        clubMemberDtoForRoleId3.setClub_id(id);
        clubMemberDtoForRoleId3.setUser_id(userDto.getId());
        clubMemberDtoForRoleId3.setRole_id(3);
        clubService.joinClub(clubMemberDtoForRoleId3);
       
        return "redirect:https://basecamp.bcyeon.click/club/home?id=" + id;
    }


    @RequestMapping("writePost")
    public String writePost(@RequestParam("id") int id, Model model){
        List<ClubPostCategoryDto> postCategoryDtoList = clubService.findPostCategory();
        model.addAttribute("id", id);
        model.addAttribute("postCategoryDtoList", postCategoryDtoList);
        return "club/writePostPage";
    }

    @RequestMapping("writePostProcess")
    public String writePostProcess(ClubPostDto clubPostDto, @RequestParam("main_image") MultipartFile[]main_image){
    
        List<ImageDto> imgList = ImageUtil.saveImageAndReturnDtoList(main_image);
        if(imgList == null || imgList.isEmpty()){
            imgList = new ArrayList<>();
        }

        clubService.writeClubPost(clubPostDto, imgList);

        return "redirect:https://basecamp.bcyeon.click/club/board?id=" + clubPostDto.getClub_id();
    }

    
    @RequestMapping("board")
    public String clubBoard(@RequestParam("id") int id, Model model, HttpSession session){
        model.addAttribute("id", id);
        
        List<Map<String,Object>> postDetailList = clubService.getClubPostDtoList(id);
        model.addAttribute("postDetailList", postDetailList);

        ClubDto clubDto = clubSqlMapper.selectClubDtoById(id);
        model.addAttribute("clubDto", clubDto);

        List<CategoryDto> categoryList = clubSqlMapper.selectCategoryList();
        model.addAttribute("categoryList", categoryList);

        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        ClubMemberDto clubMemberDto = new ClubMemberDto();
        if(userDto != null){
            clubMemberDto.setClub_id(id);
            clubMemberDto.setUser_id(userDto.getId());
            int isMemberInClub = clubService.checkClubMembership(clubMemberDto);
            model.addAttribute("isMemberInClub", isMemberInClub);
        }
        

        return "club/clubBoardPage";
    }

    @RequestMapping("album")
    public String clubAlbum(@RequestParam("id") int id, Model model){
        model.addAttribute("id", id);
        List<ClubPostImageDto> postImageDtoList = clubService.selectPostImageDtoByPostId(id);
        model.addAttribute("postImageDtoList", postImageDtoList);
        ClubDto clubDto = clubSqlMapper.selectClubDtoById(id);
        model.addAttribute("clubDto", clubDto);


        return "club/clubAlbumPage";
    }

    @RequestMapping("readPost")
    public String readPost(@RequestParam("id") int id, Model model, HttpSession session){
        model.addAttribute("id", id);
        Map<String, Object> map = clubService.getClubPostData(id);
        model.addAttribute("map", map);
        List<Map<String, Object>> postCommentDetailList = clubService.getPostCommentDetailList(id);
        clubService.increaseReadCount(id);

       List<ClubPostImageDto> clubPostImageDtoList = clubService.getPostImageDtoListById(id);
       model.addAttribute("clubPostImageDtoList", clubPostImageDtoList);


        int totalReadCount = clubService.totalReadCount(id);
        model.addAttribute("totalReadCount", totalReadCount);


        model.addAttribute("postCommentDetailList", postCommentDetailList);

        ClubPostLikeDto clubPostLikeDto = new ClubPostLikeDto();
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        if(userDto != null){
            clubPostLikeDto.setUser_id(userDto.getId());
            clubPostLikeDto.setPost_id(id);
            int confirmPostLike = clubService.confirmPostLike(clubPostLikeDto);
            model.addAttribute("confirmPostLike", confirmPostLike);
        }


        return "club/readPostPage";
    }

    @RequestMapping("writeCommentProcess")
    public String writeComment(ClubPostCommentDto clubPostCommentDto){
        clubService.writeClubPostComment(clubPostCommentDto);
    
        return "redirect:https://basecamp.bcyeon.click/club/readPost?id="+ clubPostCommentDto.getPost_id();
    }

    @RequestMapping("newClubs")
    public String newClubs(Model model){
        List<ClubDto> clubDtoList = clubService.findClubDtoList();
        model.addAttribute("clubDtoList", clubDtoList);

        return "club/newClubsListPage";
    }


    @RequestMapping("bookmarkedClubs")
    public String localClubs(HttpSession session, Model model){
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        List<Map<String, Object>> bookmarkedClubDataList = clubService.getBookmarkedClubDtoList(userDto.getId());
        model.addAttribute("bookmarkedClubDataList", bookmarkedClubDataList);

        return "club/bookmarkClubListPage";
    }

    @RequestMapping("myClubs")
    public String myClubs(HttpSession session, Model model){
        List<Map<String,Object>> myClubsDataList = new ArrayList<>();
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        List<ClubDto> joinClubDtoList = clubService.findJoinClubDtoList(userDto.getId());
        
        for(ClubDto clubDto : joinClubDtoList){
            Map<String, Object> myClubListMap = new HashMap<>();
           int region_id = clubDto.getRegion_id();
           int totalMeetings = clubService.countTotalMeeting( clubDto.getId());

           ClubRegionCategoryDto clubRegionCategoryDto = clubService.findRegionCategoryDtoById(region_id);
            myClubListMap.put("clubRegionCategoryDto", clubRegionCategoryDto);
            myClubListMap.put("clubDto", clubDto);
            myClubListMap.put("totalMeetings", totalMeetings);

            myClubsDataList.add(myClubListMap);


        }
        

        model.addAttribute("myClubsDataList", myClubsDataList);
        model.addAttribute("userDto", userDto);
        

        return "club/myClubsListPage";
    }




    @RequestMapping("bookmarkProcess")
    public String bookmarkProcess(ClubBookmarkDto clubBookmarkDto){
        int bookmarkCount = clubService.confirmBookmark(clubBookmarkDto);

        if(bookmarkCount == 0){
            clubService.insertBookmark(clubBookmarkDto);
        }else{
            clubService.delteBookmarkDto(clubBookmarkDto);
        }
        return "redirect:https://basecamp.bcyeon.click/club/home?id="+ clubBookmarkDto.getClub_id();
    }

    @RequestMapping("postLikeProcess")
    public String postLikeProcess(ClubPostLikeDto clubPostLikeDto){

        System.out.println(clubPostLikeDto);
        int postLikeCount = clubService.confirmPostLike(clubPostLikeDto);
        System.out.println(postLikeCount);

        if(postLikeCount == 0){
            clubService.insertPostLike(clubPostLikeDto);
        }else{
            clubService.deletePostLike(clubPostLikeDto);
        }
        
        return "redirect:https://basecamp.bcyeon.click/club/readPost?id=" + clubPostLikeDto.getPost_id();
        
    }


//  정모 개설하기
    @RequestMapping("createNewMeeting")
    public String createNewMeeting(@RequestParam("id") int id, Model model){
        model.addAttribute("clubId", id);
        return "club/createNewMeetingPage";
    }

    @RequestMapping("createNewMeetingProcess")
    public String createNewMeetingProcess(ClubMeetingDto clubMeetingDto,
                                          Model model,
                                          @RequestParam("main_img")
                                          MultipartFile main_img){
        String mainImageUrl = ImageUtil.saveImageAndReturnLocation(main_img);
        clubMeetingDto.setMain_image(mainImageUrl);
        clubService.insertClubMeetingDto(clubMeetingDto);

        ClubMeetingMemberDto meetingMemberDto = new ClubMeetingMemberDto();
        meetingMemberDto.setMeeting_id(clubMeetingDto.getId());
        meetingMemberDto.setUser_id(clubMeetingDto.getUser_id());

        clubService.joinMeeting(meetingMemberDto);

        ClubDto clubDto = clubService.selectClubDtoById(clubMeetingDto.getClub_id());
        model.addAttribute("clubDto", clubDto);
        return "redirect:https://basecamp.bcyeon.click/club/home?id="+clubMeetingDto.getClub_id();
    }

    // 정모 신청

    public String joinMeetingProcess(ClubMeetingMemberDto clubMeetingMemberDto){
        clubService.joinMeeting(clubMeetingMemberDto);
        

        return "redirect:https://basecamp.bcyeon.click/club/home?id=";
    }

    // 소모일 관리
    @RequestMapping("managementClub")
    public String managementClub(@RequestParam("id")int id, Model model){
        int totalMember = clubService.countTotalClubMember(id);
        Map<String, Object> clubDetail = clubService.clubDetail(id);
        int yesterdayNewPosts = clubService.countYesterdayPost(id);
        int yesterdayNewMembers = clubService.countYesterdayNewMembers(id);
        int yesterdayVisitCount = clubService.countYesterdayVisit(id);

        model.addAttribute("totalMember", totalMember);
        model.addAttribute("clubDetail", clubDetail);
        model.addAttribute("yesterdayNewPosts", yesterdayNewPosts);
        model.addAttribute("yesterdayNewMembers", yesterdayNewMembers);
        model.addAttribute("yesterdayVisitCount", yesterdayVisitCount);

        return "club/managementClubPage";
    }

    @RequestMapping("managementStaff")
    public String managementStaff(@RequestParam("id") int id, Model model){

        List<Map<String, Object>> memberList = clubService.findClubMemerDataList(id);

        model.addAttribute("id", id);
        model.addAttribute("memberList", memberList);

        return "club/managementStaffPage";
    }

    @RequestMapping("managementStaffLevel")
    public String managementStaffLevel(@RequestParam("club_id") int id, @RequestParam("user_id") int user_id){

        return "club/staffLevelPage";
    }
    
    @RequestMapping("meeting")
    public String meeting(@RequestParam("id") int id, Model model, HttpSession session){
        model.addAttribute("id", id);
        ClubDto clubDto = clubService.selectClubDtoById(id);
        model.addAttribute("clubDto", clubDto);

        
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        ClubMemberDto clubMemberDto = new ClubMemberDto();
        clubMemberDto.setClub_id(id);
        clubMemberDto.setUser_id(userDto.getId());

        int isMemberInClub = clubSqlMapper.checkClubMembership(clubMemberDto);
        model.addAttribute("isMemberInClub", isMemberInClub);


        // int isMemberInClub = clubSqlMapper.checkClubMembership(clubMemberDto);
        return "club/clubMeetingPage";
    }


   


}


