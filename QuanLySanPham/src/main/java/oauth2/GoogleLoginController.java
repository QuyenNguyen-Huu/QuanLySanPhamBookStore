package oauth2;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class GoogleLoginController {

    private final String clientId = "YOUR_GOOGLE_CLIENT_ID";
    private final String redirectUri = "http://sachhay-env.eba-xzqbixdm.ap-southeast-2.elasticbeanstalk.com/oauth2/callback/google";
    private final String scope = "openid email profile";
    private final String authorizationEndpoint = "https://accounts.google.com/o/oauth2/v2/auth";

    @GetMapping("/oauth2/google")
    public void loginWithGoogle(HttpServletResponse response) throws IOException {
        String url = authorizationEndpoint +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=" + scope +
                "&access_type=offline" +
                "&prompt=consent";

        response.sendRedirect(url);
    }
}
