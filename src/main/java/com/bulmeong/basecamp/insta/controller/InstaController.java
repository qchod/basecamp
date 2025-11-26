package com.bulmeong.basecamp.insta.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.thymeleaf.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.text.SimpleDateFormat;

import com.bulmeong.basecamp.common.util.ImageUtil;
import com.bulmeong.basecamp.common.util.Utils;
import com.bulmeong.basecamp.insta.dto.InstaArticleDto;
import com.bulmeong.basecamp.insta.dto.InstaArticleImgDto;
import com.bulmeong.basecamp.insta.dto.InstaArticleLikeDto;
import com.bulmeong.basecamp.insta.dto.InstaBookmarkDto;
import com.bulmeong.basecamp.insta.dto.InstaTagDto;
import com.bulmeong.basecamp.insta.dto.InstaUserInfoDto;
import com.bulmeong.basecamp.insta.service.InstaService;
import com.bulmeong.basecamp.user.dto.UserDto;

import jakarta.servlet.http.HttpSession;

import java.util.*;

@Controller
@RequestMapping("insta")
public class InstaController {
    // @Autowired
    // private HttpServletRequest request;
    @Autowired
    private Utils util;
    @Autowired
    private InstaService instaService;

    // insta용 임시 loginPage
    @RequestMapping("instaLoginPage")
    public String instaLoginPage(){
        

        return "insta/instaLoginPage";
    }

    // insta 접속 할 때 두가지 접속 경로가 있음
    @RequestMapping("instaLoginProcess")
    public String instaLoginProcess(HttpSession session, @RequestParam("id") int id){
        util.loginUser(id);
        UserDto userDto = (UserDto) session.getAttribute("sessionUserInfo");
        System.out.println("userDto" + userDto);

        int userC = instaService.instaUserC(userDto);

        // System.out.println("userC" + userC);

        if(userC == 1){
            // return "redirect:./instaMainPage?user_id=" + userDto.getId();
            return "redirect:https://basecamp.bcyeon.click/insta/instaMainPage?user_id=" + userDto.getId();
        }else{
            // return "redirect:./instaConfirmPage";
            return "redirect:https://basecamp.bcyeon.click/insta/instaConfirmPage";
        }

    }

    // 고객이 인스타 최초 접속시 나타나는 Page
    @RequestMapping("instaConfirmPage")
    public String instaConfirmPage(UserDto userDto){

        return "insta/instaConfirmPage";
    }

    

    @RequestMapping("confirmProcess") // MultipartFile = 한개의 파일만 받을 때 / MultipartFile[] = 여러개의 파일만 받을 때
    public String confirmProcess(InstaUserInfoDto instaUserInfoDto, @RequestParam("insta_img") MultipartFile insta_img){

        if (insta_img == null || insta_img.isEmpty()) {
            try {
                // 기본 이미지의 실제 파일 시스템 경로를 가져옴
                ClassPathResource resource = new ClassPathResource("static/img/common/default_profile.png");
                File sourceFile = resource.getFile();
                Path sourcePath = sourceFile.toPath();

                // 고유한 파일명 생성 (UUID 사용)
                String uniqueFileName = UUID.randomUUID().toString() + "_default_profile.png";
                Path destinationPath = Paths.get("src/main/resources/static/img/user_profiles/" + uniqueFileName);

                // 기본 이미지를 고유한 이름으로 복사하여 저장
                Files.copy(sourcePath, destinationPath);

                // 사용자에게 고유한 경로로 설정 (웹 경로로 설정)
                instaUserInfoDto.setInsta_profile_img("/img/user_profiles/" + uniqueFileName);
            } catch (IOException e) {
                e.printStackTrace();
                // 복사 실패 시 기본 이미지 경로 설정
                instaUserInfoDto.setInsta_profile_img("/img/common/default_profile.png");
            }
        }else{
            instaUserInfoDto.setInsta_profile_img(ImageUtil.saveImageAndReturnLocation(insta_img));
        }

        System.out.println(instaUserInfoDto.getInsta_profile_img());

        instaService.instaUserInfo(instaUserInfoDto);
        
        // return "insta/confirmProcess";
        // return "redirect:./instaMainPage?user_id=" + instaUserInfoDto.getUser_id();
        return "redirect:https://basecamp.bcyeon.click/insta/instaMainPage?user_id=" + instaUserInfoDto.getUser_id();
    }

    // 인스타 메인페이지
    @RequestMapping("instaMainPage")
    public String instaMainPage(Model model, HttpSession session){

        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");

        if(userDto != null){ // 로그인 된 상태

            int s_user_id = userDto.getId();

            InstaUserInfoDto instaUserInfoDto = instaService.userInfoByUserId(s_user_id);

            if(instaUserInfoDto != null){ // 인스타에 프로필 등록 된 유저
                model.addAttribute("instaUserInfoDto", instaUserInfoDto);

                InstaArticleLikeDto instaArticleLikeDto = new InstaArticleLikeDto();
                instaArticleLikeDto.setUser_id(instaUserInfoDto.getId());

                // System.out.println(instaArticleListAll);
                List<Map<String, Object>> instaArticleListAll = instaService.selectInstaArticleList(instaUserInfoDto.getId());
                model.addAttribute("instaArticleListAll", instaArticleListAll);

                // 인스스 _ 로그인 유저가 팔로우 한 유저 출력
                List<InstaUserInfoDto> instaStoryList = instaService.likeInstaStoryButIsNotSoSad(instaUserInfoDto.getId());
                model.addAttribute("instaStoryList", instaStoryList);
                

                return "insta/instaMainPage";
            }else{ // 인스타에 프로필 등록 안 된 유저

                // return "redirect:./instaConfirmPage";
                return "redirect:https://basecamp.bcyeon.click/insta/instaConfirmPage";
            }
            
        }else{ // 로그인 안 된 상태

            // return "redirect:./user/login";
            return "redirect:https://basecamp.bcyeon.click/user/login";
        }
        
    }

    // 좋아요
    @RequestMapping("pushArticleLikeProcess")
    public String pushArticleLikeProcess(HttpSession session, InstaArticleLikeDto instaArticleLikeDto){
        UserDto userDto = (UserDto) session.getAttribute("sessionUserInfo");

        instaService.like(instaArticleLikeDto);

        // return "redirect:./instaMainPage?user_id=" + userDto.getId();
        return "redirect:https://basecamp.bcyeon.click/insta/instaMainPage?user_id=" + userDto.getId();
    }

    @RequestMapping("pushArticleLikeDeleteProcess")
    public String pushArticleLikeDeleteProcess(HttpSession session, InstaArticleLikeDto instaArticleLikeDto){
        UserDto userDto = (UserDto) session.getAttribute("sessionUserInfo");

        instaService.unLike(instaArticleLikeDto);

        // return "redirect:./instaMainPage?user_id=" + userDto.getId();
        return "redirect:https://basecamp.bcyeon.click/insta/instaMainPage?user_id=" + userDto.getId();
    }

    @RequestMapping("instaWritePage")
    public String instaWritePage(Model model, @RequestParam("user_id") int id){
        InstaUserInfoDto instaUserInfoDto = instaService.selectUserInfo(id);
        model.addAttribute("instaUserInfoDto", instaUserInfoDto);

        return "insta/instaWritePage";
    }

    
    @RequestMapping("instaWriteProcess")
    public String instaWriteProcess(InstaArticleDto instaArticleDto, @RequestParam("text") String text,
                                    @RequestParam("insta") MultipartFile[]insta_article_img){

        instaService.writeArticle(instaArticleDto, text);

        InstaUserInfoDto instaUserInfoDto = instaService.selectUserInfo(instaArticleDto.getUser_id());

        List<InstaArticleImgDto> instaAtricleImgDtoList = new ArrayList<>();

        // 원래 내 코드(준희님 유틸 사용)
        // List<ImageDto> instaImgList = ImageUtil.saveImageAndReturnDtoList(insta_article_img);
        // for(ImageDto imageDto : instaImgList){
        //     InstaArticleImgDto instaArticleImgDto = new InstaArticleImgDto();
        //     instaArticleImgDto.setImg_link(imageDto.getLocation());
        //     instaArticleImgDto.setArticle_id(instaArticleDto.getId());
        //     instaAtricleImgDtoList.add(instaArticleImgDto);
        // }

        // 규진이 이미지 유틸 사용
        if (insta_article_img != null) {
            for (int i = 0; i < insta_article_img.length; i++) {
                MultipartFile image = insta_article_img[i];
                if (image.isEmpty()) {
                    continue;
                }
                String rootPath = "/basecampImage/";

                // 날짜 폴더
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
                String todayPath = sdf.format(new Date());

                File todayFolderForCreate = new File(rootPath + todayPath);

                if (!todayFolderForCreate.exists()) {
                    todayFolderForCreate.mkdirs();
                }

                // 파일 충돌 방지
                String originalFileName = image.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                long currentTime = System.currentTimeMillis();
                String fileName = uuid + "_" + currentTime;
                // 확장자 추출
                String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
                fileName += ext;

                try {
                    image.transferTo(new File(rootPath + todayPath + fileName));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 첫 번째 이미지를 메인 이미지로 설정
                // if (i == 0) {
                //     secondhandProductDto.setMain_image(todayPath + fileName);
                // }
                // DB 저장 DTO
                InstaArticleImgDto instaArticleImgDto = new InstaArticleImgDto();
                instaArticleImgDto.setImg_link(todayPath + fileName);
                instaArticleImgDto.setArticle_id(instaArticleDto.getId());
                instaAtricleImgDtoList.add(instaArticleImgDto);
            }

        }

        instaService.writeArticleImg(instaAtricleImgDtoList);

        // return "redirect:./instaMainPage?user_id=" + instaUserInfoDto.getUser_id();
        return "redirect:https://basecamp.bcyeon.click/insta/instaMainPage?user_id=" + instaUserInfoDto.getUser_id();
    }

    @RequestMapping("commentWritePage")
    public String commentWritePage(Model model, @RequestParam("user_id") int id){
        InstaUserInfoDto instaUserInfoDto = instaService.selectUserInfo(id);

        model.addAttribute("instaUserInfoDto", instaUserInfoDto);

        return "insta/commentWritePage";
    }

    @RequestMapping("instaMyPage")
    public String instaMyPage(Model model, @RequestParam("user_id") int id){ // id = userInfoDto id
        InstaUserInfoDto instaUserInfoDto = instaService.selectUserInfo(id);
        model.addAttribute("instaUserInfoDto", instaUserInfoDto);

        // html escape _ 개행
        String escapedIntro = StringUtils.escapeXml(instaUserInfoDto.getIntro());
        escapedIntro = escapedIntro.replaceAll("\n", "<br>");
        instaUserInfoDto.setIntro(escapedIntro);


        int articleCount = instaService.selectArticleCountByUserId(id);
        model.addAttribute("articleCount", articleCount);

        // 몇번 회원이 몇명을 팔로우 했는지
        int followerCount = instaService.followerCount(id);
        model.addAttribute("followerCount", followerCount);

        // 몇번 회원을 몇명이 팔로잉 했는지
        int followingCount = instaService.followingCount(id);
        model.addAttribute("followingCount", followingCount);

        // 유저가 작성한 게시글 이미지 출력
        List<InstaArticleImgDto> instaArticleImgDtoList = instaService.getInstaWriteArticleImgList(id); // id = user_id
        model.addAttribute("instaArticleImgDtoList", instaArticleImgDtoList);

        return "insta/instaMyPage";
    }

    @RequestMapping("instaBookmarkPage")
    public String instaBookmarkPage(Model model, @RequestParam("user_id") int id){
        InstaUserInfoDto instaUserInfoDto = instaService.selectUserInfo(id);
        model.addAttribute("instaUserInfoDto", instaUserInfoDto);

        // html escape _ 개행
        String escapedIntro = StringUtils.escapeXml(instaUserInfoDto.getIntro());
        escapedIntro = escapedIntro.replaceAll("\n", "<br>");
        instaUserInfoDto.setIntro(escapedIntro);


        int articleCount = instaService.selectArticleCountByUserId(id);
        model.addAttribute("articleCount", articleCount);

        // 몇번 회원이 몇명을 팔로우 했는지
        int followerCount = instaService.followerCount(id);
        model.addAttribute("followerCount", followerCount);

        // 몇번 회원을 몇명이 팔로잉 했는지
        int followingCount = instaService.followingCount(id);
        model.addAttribute("followingCount", followingCount);

        // 유저가 저장한 북마크 게시글 이미지List 출력
        List<InstaArticleImgDto> instaBookmarkArticleImgDtoList = instaService.getInstaSaveBookmarkImgList(id);
        model.addAttribute("instaBookmarkArticleImgDtoList", instaBookmarkArticleImgDtoList);

        return "insta/instaBookmarkPage";
    }

    @RequestMapping("pushBookmarkProcess")
    public String pushBookmarkProcess(HttpSession session, InstaBookmarkDto instaBookmarkDto){
        UserDto userDto = (UserDto) session.getAttribute("sessionUserInfo");

        instaService.insertBookmark(instaBookmarkDto);

        // return "redirect:./instaMainPage?user_id=" + userDto.getId();
        return "redirect:https://basecamp.bcyeon.click/insta/instaMainPage?user_id=" + userDto.getId();
    }

    @RequestMapping("pushBookmarkDeleteProcess")
    public String pushBookmarkDeleteProcess(HttpSession session, InstaBookmarkDto instaBookmarkDto){
        UserDto userDto = (UserDto) session.getAttribute("sessionUserInfo");

        instaService.deleteBookmark(instaBookmarkDto);
        
        // return "redirect:./instaMainPage?user_id=" + userDto.getId();
        return "redirect:https://basecamp.bcyeon.click/insta/instaMainPage?user_id=" + userDto.getId();
    }
    
    @RequestMapping("instaUserPage")
    public String instaUserPage(Model model, @RequestParam("user_id") int id){ // id = userInfoDto id
        InstaUserInfoDto instaUserInfoDto = instaService.selectUserInfo(id);

        // html escape _ 개행
        String escapedIntro = StringUtils.escapeXml(instaUserInfoDto.getIntro());
        escapedIntro = escapedIntro.replaceAll("\n", "<br>");
        instaUserInfoDto.setIntro(escapedIntro);

        

        int articleCount = instaService.selectArticleCountByUserId(id);
        model.addAttribute("articleCount", articleCount);

        // 몇번 회원이 몇명을 팔로우 했는지
        int followerCount = instaService.followerCount(id);
        model.addAttribute("followerCount", followerCount);

        model.addAttribute("instaUserInfoDto", instaUserInfoDto);

        // 유저가 작성한 게시글 이미지 출력
        List<InstaArticleImgDto> instaArticleImgDtoList = instaService.getInstaWriteArticleImgList(id); // id = user_id
        model.addAttribute("instaArticleImgDtoList", instaArticleImgDtoList);

        return "insta/instaUserPage";
    }
    
    @RequestMapping("instaUserDetailPage")
    public String instaUserDetailPage(){

        return "insta/instaUserDetailPage";
    }

    @RequestMapping("pushLikeUserPage")
    public String pushLikeUserPage(Model model, @RequestParam("article_id") int article_id,
                                                @RequestParam("user_id") int user_id){ // user_id = session 유저 아이디

        InstaUserInfoDto instaUserInfoDto = instaService.userInfoByUserId(user_id);
        System.out.println(instaUserInfoDto.getId());

        List<Map<String, Object>> instaUserInfoDtoList = instaService.selectArticleLikeUserInfo(article_id, instaUserInfoDto.getId());
        model.addAttribute("instaUserInfoDtoList", instaUserInfoDtoList);

        return "insta/pushLikeUserPage";
    }

    @RequestMapping("userFollowersPage")
    public String userFollowersPage(){

        return "insta/userFollowersPage";
    }

    @RequestMapping("userFollowingPage")
    public String userFollowingPage(){

        return "insta/userFollowingPage";
    }

    @RequestMapping("articleDeleteProcess")
    public String articleDeleteProcess(@RequestParam("article_id") int id, @RequestParam("user_id") int user_id){
        instaService.deleteArticle(id);

        // return "redirect:./instaMainPage?user_id=" + user_id;
        return "redirect:https://basecamp.bcyeon.click/insta/instaMainPage?user_id=" + user_id;
    }

    // 검색 페이지
    @RequestMapping("instaSearchPage")
    public String instaSearchPage(Model model, @RequestParam("user_id") int id) { // user_id = 로그인 한 인스타 유저 id

        List<Map<String, Object>> recentSearchList = instaService.recentSearch(id);
        model.addAttribute("recentSearchList", recentSearchList);

        // 이렇게 하는거 맞냐..
        // instaSearchProcess로 파라미터 넘겨주기위해 model에 담았음
        model.addAttribute("user_id", id);

        return "insta/instaSearchPage";
    }

    // 검색 프로세스
    @RequestMapping("instaSearchProcess")
    public String instaSearchProcess(Model model, @RequestParam("searchWord") String searchWord , @RequestParam("user_id") int user_id) { // user_id = 로그인 한 인스타 유저 id
        // 일반검색
        instaService.insertSearchContentOrTag(searchWord, user_id);

        // URL 인코딩을 사용하여 한글 처리
        String encodedContent = URLEncoder.encode(searchWord, StandardCharsets.UTF_8);
        

        // return "redirect:./instaSearchResultPage?searchWord=" + encodedContent + "&user_id=" + user_id;
        return "redirect:https://basecamp.bcyeon.click/insta/instaSearchResultPage?searchWord=" + encodedContent + "&user_id=" + user_id;
    }

    // 검색 결과 페이지 _ 일반 검색과 해시태그 검색 두가지로 분기
    @RequestMapping("instaSearchResultPage")
    public String instaSearchResultPage(HttpSession session, Model model, @RequestParam("searchWord") String content){
        // URL 디코딩
        content = URLDecoder.decode(content, StandardCharsets.UTF_8);

        UserDto userDto = (UserDto) session.getAttribute("sessionUserInfo");
        InstaUserInfoDto instaUserInfoDto = instaService.userInfoByUserId(userDto.getId());

        // 태그 검색인지 일반 검색인지 확인
        if(content.startsWith("#")){
            
            InstaTagDto instaTagDto = instaService.selectTagDto(content);
            

            if(instaTagDto != null){
                model.addAttribute("instaTagDto", instaTagDto);

                List<InstaArticleImgDto> instaArticleImgDtoListByTagText = instaService.selectArticleImgBySearchTagText(content);

                model.addAttribute("instaArticleImgDtoListByTagText", instaArticleImgDtoListByTagText);

                return "insta/instaTagSearchResultPage";

            }else{ // 검색 결과 없음

                return "insta/instaSearchResultNullPage";
            }
            
        }else{

            String selectResultContent = instaService.selectResultContent(content, instaUserInfoDto.getId());
            model.addAttribute("selectResultContent", selectResultContent);
            

            // 일반 검색 결과
            List<InstaArticleImgDto> instaArticleImgDtoListBySearchContent = instaService.selectArticleImgFromSearchContent(content);

            /* 메서드에서 null을 반환하는게 아니라 빈 리스트를 반환해서 instaArticleImgDtoListBySearchContent != null 이라고 입력하는게 의미가 없음
               isEmpty()로 입력해 줘야 원하는 결과를 출력 할 수 있음 */
            if(instaArticleImgDtoListBySearchContent.isEmpty()){ // 검색 결과 없음

                return "insta/instaSearchResultNullPage";

            }else{

                model.addAttribute("instaArticleImgDtoListBySearchContent", instaArticleImgDtoListBySearchContent);

                // 일반 검색 결과 페이지로 리턴
                return "insta/instaSearchResultPage"; 
            }

        }

    }


    // 해시태그 클릭시 결과 페이지
    @RequestMapping("instaSearchHashTagClickResultPage")
    public String instaSearchHashTagClickResultPage(Model model, @RequestParam("tag_id") int tag_id, HttpSession session){
        UserDto userDto = (UserDto) session.getAttribute("sessionUserInfo");
        InstaUserInfoDto instaUserInfoDto = instaService.userInfoByUserId(userDto.getId());

        instaService.insertSearchTagId(tag_id, instaUserInfoDto.getId());

        // 검색이 아니고 태그 클릭 시 나오는 페이지
        List<InstaArticleImgDto> instaArticleImgDtoList = instaService.selectArticleFirstImg(tag_id);

        // 어떤 태그 검색했는지 태그 아이디 받아서 text 뽑는 쿼리
        InstaTagDto instaTagDto = instaService.selectTag(tag_id);
        model.addAttribute("instaTagDto", instaTagDto);

        model.addAttribute("instaArticleImgDtoList", instaArticleImgDtoList);

        return "insta/instaSearchHashTagClickResultPage";
    }

    // content로 일반 검색 삭제
    @RequestMapping("recentSearchDeleteContentProcess")
    public String recentSearchDeleteContentProcess(@RequestParam("content") String content, @RequestParam("user_id") int user_id){ // user_id = 세션 아이디
        InstaUserInfoDto instaUserInfoDto = instaService.userInfoByUserId(user_id);

        instaService.recentSearchDeleteContent(content, instaUserInfoDto.getId());

        // return "redirect:./instaSearchPage?user_id=" + instaUserInfoDto.getId();
        return "redirect:https://basecamp.bcyeon.click/insta/instaSearchPage?user_id=" + instaUserInfoDto.getId();
    }

    // 태그 id로 태그 검색 삭제
    @RequestMapping("recentSearchDeleteTagProcess")
    public String recentSearchDeleteTagProcess(@RequestParam("tag_id") int tag_id, @RequestParam("user_id") int user_id){ // user_id = 세션 아이디
        InstaUserInfoDto instaUserInfoDto = instaService.userInfoByUserId(user_id);

        instaService.recentSearchDeleteTag(tag_id, instaUserInfoDto.getId());

        // return "redirect:./instaSearchPage?user_id=" + instaUserInfoDto.getId();
        return "redirect:https://basecamp.bcyeon.click/insta/instaSearchPage?user_id=" + instaUserInfoDto.getId();
    }

    // 검색 기록 전체 삭제
    @RequestMapping("recentSearchAllDeleteProcess")
    public String recentSearchAllDeleteProcess(@RequestParam("user_id") int user_id){ // user_id = 세션 아이디
        InstaUserInfoDto instaUserInfoDto = instaService.userInfoByUserId(user_id);

        instaService.recentSearchAllDelete(instaUserInfoDto.getId());

        // return "redirect:./instaSearchPage?user_id=" + instaUserInfoDto.getId();
        return "redirect:https://basecamp.bcyeon.click/insta/instaSearchPage?user_id=" + instaUserInfoDto.getId();
    }

    // 인사이트 _ 통계
    @RequestMapping("instaStatsPage")
    public String instaStatsPage(@RequestParam("user_id") int s_user_id){ // user_id = 세션 아이디
        InstaUserInfoDto instaUserInfoDto = instaService.userInfoByUserId(s_user_id);

        return "insta/instaStatsPage?user_id=" + instaUserInfoDto.getId();
    }

    // 프로필 편집
    @RequestMapping("instaProfileEditPage")
    public String instaProfileEditPage(){

        return "insta/instaProfileEditPage";
    }

    // 상세 글 페이지
    @RequestMapping("instaArticleDetailPage")
    public String instaArticleDetailPage(Model model, @RequestParam("article_id") int article_id) { // user_id = sessionId

        // 유저프로필, 닉네임, 게시글 내용
        Map<String, Object> articleInfo = instaService.articleDtailPageSelectArticleInfo(article_id);
        model.addAttribute("articleInfo", articleInfo);

        // 게시물 이미지 List
        List<InstaArticleImgDto> instaArticleImgDtoList = instaService.selectArticleImgList(article_id);
        model.addAttribute("instaArticleImgDtoList", instaArticleImgDtoList);

        // 게시물 좋아요 수
        int articleLikeCount = instaService.getLikeTotalCount(article_id);
        model.addAttribute("articleLikeCount", articleLikeCount);

        // 게시물 태그List
        List<InstaTagDto> instaTagDtoList = instaService.selectTagTextList(article_id);
        model.addAttribute("instaTagDtoList", instaTagDtoList);

        // 게시물 댓글 수
        int articleCommentCount = instaService.articleCommentCount(article_id);
        model.addAttribute("articleCommentCount", articleCommentCount);


        return "insta/instaArticleDetailPage";
    }
}




