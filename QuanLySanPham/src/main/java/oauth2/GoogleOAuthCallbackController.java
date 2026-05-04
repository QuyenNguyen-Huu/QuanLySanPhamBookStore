package oauth2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.User;  // 🚀 import User class của bạn!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Controller
public class GoogleOAuthCallbackController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/oauth2/callback/google")
    public void handleGoogleCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        if (code == null) {
            response.getWriter().write("Lỗi: Không có code!");
            return;
        }

        // 1 Lấy access_token
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String params = "code=" + code +
                "&client_id=YOUR_CLIENT_ID" +
                "&client_secret=YOUR_CLIENT_SECRET" +
                "&redirect_uri=http://sachhay-env.eba-xzqbixdm.ap-southeast-2.elasticbeanstalk.com/oauth2/callback/google" +
                "&grant_type=authorization_code";

        try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
            writer.write(params);
            writer.flush();
        }

        StringBuilder resp = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                resp.append(line);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonMap = mapper.readValue(resp.toString(), HashMap.class);
        String accessToken = (String) jsonMap.get("access_token");

        // 2️ Lấy thông tin user từ Google
        URL userInfoUrl = new URL("https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken);
        HttpURLConnection userInfoConn = (HttpURLConnection) userInfoUrl.openConnection();
        userInfoConn.setRequestMethod("GET");

        StringBuilder userResp = new StringBuilder();
        try (BufferedReader userReader = new BufferedReader(new InputStreamReader(userInfoConn.getInputStream()))) {
            String line;
            while ((line = userReader.readLine()) != null) {
                userResp.append(line);
            }
        }

        // 3️ Parse JSON user info
        Map<String, Object> userMap = mapper.readValue(userResp.toString(), HashMap.class);
        System.out.println("User Info: " + userMap);

        // 4️ Tạo User object
        User user = new User();
        user.setUsername((String) userMap.get("name"));
        user.setEmail((String) userMap.get("email"));
        user.setRole("USER");  // mặc định USER

        //  lưu avatar
        // user.setPicture((String) userMap.get("picture"));

        // 5️ Kiểm tra xem user đã tồn tại chưa (theo email)
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(user.getEmail()));
        User existingUser = mongoTemplate.findOne(query, User.class);

        if (existingUser == null) {
            // Chưa tồn tại, lưu vào MongoDB
            mongoTemplate.save(user, "users");
            System.out.println(" Đã lưu user mới vào MongoDB: " + user.getEmail());
        } else {
            System.out.println(" User đã tồn tại: " + existingUser.getEmail());
            user = existingUser;  // Lấy user từ DB để tránh id khác nhau
        }

        
     // 6️ Xóa session cũ và tạo mới (tránh giữ HashMap cũ)
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession newSession = request.getSession(true);  // tạo mới
        newSession.setAttribute("loggedInUser", user);
        // 7️ Redirect về home
        String contextPath = request.getContextPath();
        response.sendRedirect(contextPath + "/home");

        System.out.println(" Đã hoàn thành Google callback!");
    }
}
