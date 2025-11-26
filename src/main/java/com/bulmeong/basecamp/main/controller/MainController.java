package com.bulmeong.basecamp.main.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.bulmeong.basecamp.club.dto.ClubDto;
import com.bulmeong.basecamp.club.service.ClubService;
import com.bulmeong.basecamp.common.util.Utils;
import com.bulmeong.basecamp.main.model.OAuthToken;
import com.bulmeong.basecamp.store.service.StoreService;
import com.bulmeong.basecamp.user.dto.UserDto;
import com.bulmeong.basecamp.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    @Autowired
    private UserService userService;
    @Autowired
    private ClubService clubService;
    @Autowired
    private Utils utils;

    @Autowired
    private StoreService storeService;

    @GetMapping("")
    public String basecampPublicPage(HttpSession session,
                                     Model model) {
        UserDto sessionUserInfo = (UserDto) session.getAttribute("sessionUserInfo");
        model.addAttribute("sessionUserInfo", sessionUserInfo);
        List<ClubDto> clubDtoList = clubService.findClubDtoList();
        List<ClubDto> limitedClubDtoList = clubDtoList.stream()
        .limit(10)
        .collect(Collectors.toList());
        utils.setModel("clubDtoList", limitedClubDtoList);

        model.addAttribute("bestProductDataList", storeService.getThreeProductDataList());

        return "common/basecampPublicPage";
    }

    @RequestMapping("myPage")
    public String basecampMyInfoPage(HttpSession session, 
                                     Model model){
        UserDto sessionUserInfo = (UserDto) session.getAttribute("sessionUserInfo");
        model.addAttribute("sessionUserInfo", sessionUserInfo);
        
        return "common/basecampMyInfoPage";
    }

    // @GetMapping("/auth/joinForm") // 회원가입하는데 인증필요없으므로 /auth
	// public String joinForm() {
	// 	return "user/joinForm";
	// }

	// @GetMapping("/auth/loginForm")
	// public String loginForm() {
	// 	return "user/loginForm";
	// }

    @GetMapping("/auth/kakao/callback")
    public String kakaoCallback(@RequestParam("code") String code, HttpSession session){

        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", "111ec870310ec7e3890df38c0637862b");
		params.add("redirect_uri", "https://basecamp.bcyeon.click/auth/kakao/callback");
		params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = rt.exchange(
            "https://kauth.kakao.com/oauth/token", // https://{요청할 서버 주소}
            HttpMethod.POST, // 요청할 방식
            kakaoTokenRequest, // 요청할 때 보낼 데이터
            String.class // 요청 시 반환되는 데이터 타입
		);

        ObjectMapper objectMapper = new ObjectMapper();
        OAuthToken oAuthToken = null;
        try{
            oAuthToken = objectMapper.readValue(response.getBody(), OAuthToken.class);
        }catch(JsonMappingException e){
            e.printStackTrace();
        } catch(JsonProcessingException e){
            e.printStackTrace();
        }
        System.out.println("카카오 엑세스 토큰"+oAuthToken.getAccess_token());

        RestTemplate rt2 = new RestTemplate();

        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Authorization", "Bearer "+oAuthToken.getAccess_token());
		headers2.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest2 = new HttpEntity<>(headers2);

        ResponseEntity<String> response2 = rt2.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.POST,
            kakaoProfileRequest2,
            String.class
		);

        ObjectMapper objectMapper2 = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper2.readTree(response2.getBody());
            String id = rootNode.get("id").asText();
            JsonNode propertiesNode = rootNode.get("properties");
            String nickname = propertiesNode.get("nickname").asText();

            UserDto userDto = new UserDto();
            userDto.setAccount(id);
            userDto.setPassword(id);
            userDto.setNickname(nickname);
            userDto.setName(nickname);

            if(userService.isExistAccount(id)){
                session.setAttribute("sessionUserInfo", userService.getUserByAccountAndPassword(userDto));
                return "redirect:https://basecamp.bcyeon.click";
            }else{
                userService.registerKakaoUser(userDto);
                session.setAttribute("sessionUserInfo", userService.getUserByAccountAndPassword(userDto));
                return "redirect:https://basecamp.bcyeon.click";
            }
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "redirect:https://basecamp.bcyeon.click";
    }

}
