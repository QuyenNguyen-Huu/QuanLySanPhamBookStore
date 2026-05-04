package configs;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
	public WebConfig() {
        System.out.println("✅ WebConfig loaded!");
    }

	 @Override
	    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		 registry.addResourceHandler("/uploads/**")
	        .addResourceLocations("file:/C:/Users/ADMIN/Desktop/uploads/");
	    }
}