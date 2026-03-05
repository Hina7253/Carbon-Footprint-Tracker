import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class PageAuditService {

    public List<PageResource> auditPage(String url) {
        List<PageResource> resources = new ArrayList<>();

        // Chrome options - headless mode
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        // DevTools Protocol use karo network capture ke liye
        DevTools devTools = ((HasDevTools) driver).getDevTools();
        devTools.createSession();

        // Network monitoring enable karo
        devTools.send(Network.enable(
                Optional.empty(), Optional.empty(), Optional.empty()
        ));

        // Har response ko capture karo
        devTools.addListener(Network.responseReceived(), response -> {
            PageResource resource = new PageResource();
            String resourceUrl = response.getResponse().getUrl();
            double sizeKB = response.getResponse()
                    .getEncodedDataLength() / 1024.0;

            resource.setUrl(resourceUrl);
            resource.setSizeKB(sizeKB);
            resource.setResourceType(
                    detectResourceType(response.getType().toString(), resourceUrl)
            );
            resources.add(resource);
        });

        // Page load karo
        driver.get(url);

        // Page load hone do
        try { Thread.sleep(3000); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        driver.quit();
        return resources;
    }

    private String detectResourceType(String type, String url) {
        if (type.equalsIgnoreCase("Image")) return "IMAGE";
        if (type.equalsIgnoreCase("Script")) return "JAVASCRIPT";
        if (type.equalsIgnoreCase("Stylesheet")) return "CSS";
        if (type.equalsIgnoreCase("Media")) return "VIDEO";
        if (type.equalsIgnoreCase("Font")) return "FONT";
        if (type.equalsIgnoreCase("XHR") ||
                type.equalsIgnoreCase("Fetch")) return "API";
        return "OTHER";
    }
}