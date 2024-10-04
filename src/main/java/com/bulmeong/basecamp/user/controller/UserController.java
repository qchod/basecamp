package com.bulmeong.basecamp.user.controller;

import com.bulmeong.basecamp.common.util.Utils;
import com.bulmeong.basecamp.user.dto.UserDto;
import com.bulmeong.basecamp.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private Utils utils;
    @Autowired
    private UserService userService;

    @GetMapping("login")
    public String basecampLoginPage() {

        return "common/basecampLoginPage";
    }

    @GetMapping("myPage")
    public String basecampMyInfoPage() {

        return "common/basecampMyInfoPage";
    }

    @GetMapping("register")
    public String basecampSignPage() {
        int count = userService.getLastUserCount() + 1;
        utils.setModel("userCount", count);
        utils.setModel("userNick",nicknames[count-1]);
        utils.setModel("userName",names[count-1]);
        utils.setModel("email",String.format("user%d@email.com", count));
        utils.setModel("phone", String.format("010-%04d-%04d", count,count));
        return "common/basecampSignPage";
    }

    @PostMapping("registerProcess")
    public String signProcess(@ModelAttribute UserDto userDto) {
        
        userService.insertUser(userDto);
        return "redirect:https://basecamp.null-pointer-exception.com/user/login";
    }

    @PostMapping("loginProcess")
    public String loginProcess(HttpSession session,
                               @ModelAttribute UserDto userDto) {

        UserDto sessionUserInfo = userService.getUserByAccountAndPassword(userDto);
        if (sessionUserInfo == null) {
            return "common/basecampLoginFailPage";
        } else {
            session.setAttribute("sessionUserInfo", sessionUserInfo);
            // 프로젝트 끝나면 지워
            String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
            if (redirectAfterLogin != null) {
                session.removeAttribute("redirectAfterLogin"); // 사용 후 세션에서 제거
                return "redirect:https://basecamp.null-pointer-exception.com" + redirectAfterLogin;
                // return "redirect:" + redirectAfterLogin;
            }

            // return "redirect:https://basecamp.null-pointer-exception.com";
            return "redirect:/";
        }
    }

    @GetMapping("logoutProcess")
    public String logoutProcess(HttpSession session) {
        if(utils.getSession("sessionUserInfo") ==null)
            return "redirect:https://basecamp.null-pointer-exception.com/user/login";
        session.invalidate();
        return "redirect:https://basecamp.null-pointer-exception.com";
    }

    @RequestMapping("coupon")
    public String coupon(){
        return "common/coupon";
    }

    @RequestMapping("pointLog")
    public String pointLog(HttpSession session, Model model){
        
        UserDto userDto = (UserDto)session.getAttribute("sessionUserInfo");
        model.addAttribute("pointLogDtoList", userService.getUserMileageLogByUserId(userDto.getId()));

        return "common/pointLog";
    }

    private final String[] nicknames = {
        "푸른하늘", "작은별", "맑은물", "꽃향기", "바람소리",
        "기분좋은하루", "조용한아침", "별빛아래", "봄날의햇살", "가을의낙엽",
        "겨울바람", "따뜻한차", "여름바다", "행복한미소", "순수한마음",
        "산책로", "평화로운밤", "달빛속삭임", "별의꿈", "바다내음",
        "초록풀잎", "고요한저녁", "아침이슬", "나무그늘", "따뜻한커피",
        "아름다운순간", "햇살가득", "푸른숲속", "가을하늘", "맑은호수",
        "봄꽃", "여름노을", "겨울눈", "따뜻한미소", "소박한삶",
        "기억의조각", "추억의장소", "고요한마음", "편안한쉼터", "자연의소리",
        "비오는날", "맑은아침", "따뜻한햇빛", "햇빛쏟아지는", "노을빛",
        "바람따라", "눈부신날", "바다를품은", "마음속바다", "숲속의아침",
        "들꽃", "푸른강", "깊은산속", "숲속풍경", "고요한풍경",
        "잎새", "구름사이", "별이빛나는", "평온한하루", "봄의향기",
        "가을향기", "겨울풍경", "산들바람", "푸른하늘", "물방울",
        "기분좋은바람", "맑은하늘", "푸른물결", "가을의향기", "소리없는아침",
        "자연의빛", "산속풍경", "고요한밤", "노을속으로", "햇빛속에",
        "숲속소리", "바람에흔들리는", "맑은공기", "따뜻한저녁", "평온한새벽",
        "하늘빛", "바람속으로", "햇살받으며", "초록바람", "산속소리",
        "작은세상", "자연속에서", "노을에물든", "숲속바람", "나무속삭임",
        "햇빛사이", "구름속으로", "바다를향해", "산을향해", "평온한풍경",
        "바람을따라", "숲속으로", "하늘을보며", "별을보며", "바다를보며"
    };
    private final String[] names = {
        "김가온", "이나래", "박다솜", "최라온", "정마루",
        "김나현", "이도연", "박보라", "최채린", "정현서",
        "김윤아", "이서윤", "박지민", "최수연", "정지유",
        "김채원", "이은서", "박예원", "최소윤", "정서연",
        "김하윤", "이도윤", "박현우", "최민준", "정지호",
        "김서준", "이예준", "박시우", "최주원", "정하준",
        "김유진", "이하린", "박다온", "최수빈", "정예빈",
        "김준서", "이하은", "박민서", "최서준", "정윤서",
        "김시윤", "이은채", "박다은", "최주하", "정하은",
        "김민지", "이유리", "박채영", "최수현", "정나영",
        "김민아", "이연서", "박다빈", "최유빈", "정지아",
        "김다윤", "이하은", "박유진", "최채은", "정은지",
        "김민혁", "이준혁", "박서준", "최주혁", "정예린",
        "김다영", "이수민", "박유나", "최소현", "정하영",
        "김은우", "이진우", "박민수", "최동현", "정재윤",
        "김태윤", "이서율", "박지후", "최준수", "정유찬",
        "김다빈", "이시윤", "박승현", "최지후", "정다현",
        "김하연", "이하영", "박수호", "최지민", "정민지",
        "김유현", "이서진", "박재원", "최서진", "정시원",
        "김윤호", "이정훈", "박수연", "최진우", "정찬희",
        "김준호", "이재훈", "박성민", "최지성", "정도현"
    };

}
