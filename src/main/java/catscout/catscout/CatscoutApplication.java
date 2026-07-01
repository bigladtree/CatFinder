package catscout.catscout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.List;
import catscout.catscout.webscraper.CatListing;
import catscout.catscout.webscraper.platforms.ShelterluvScraper;

@SpringBootApplication
public class CatscoutApplication {

	public static void main(String[] args) {
		ShelterluvScraper scraper = new ShelterluvScraper();
		// test with byc shelter
		List<CatListing> cats = scraper.scrape("1863");
		for (CatListing cat : cats) {
			System.out.println(cat.getName() + " | " + cat.getBreed() + " | " + cat.getAge() + " | " + cat.getSex());
		}

		SpringApplication.run(CatscoutApplication.class, args);
	}

}
