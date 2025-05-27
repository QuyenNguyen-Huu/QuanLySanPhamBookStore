package configs;


import com.mongodb.client.MongoClient;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener
public class AppContextListener implements ServletContextListener  {
	@Override
    public void contextDestroyed(ServletContextEvent sce) {
        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
        if (ctx != null) {
            MongoClient mongoClient = ctx.getBean(MongoClient.class);
            if (mongoClient != null) {
                mongoClient.close(); // tránh memory leak
            }
        }
    }
	
	

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Không cần xử lý gì khi khởi tạo
    }
}
