package catscout.catscout.webscraper;

import java.util.List;

public interface ShelterScraper {
    List<CatListing> scrape(String shelterId);

    String getPlatformName();
}
