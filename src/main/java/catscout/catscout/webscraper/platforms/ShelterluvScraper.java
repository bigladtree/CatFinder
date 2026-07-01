package catscout.catscout.webscraper.platforms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import catscout.catscout.webscraper.CatListing;
import catscout.catscout.webscraper.ShelterScraper;

@Component
public class ShelterluvScraper implements ShelterScraper {

    private static final String BASE_URL = "https://www.shelterluv.com";

    @Override
    public List<CatListing> scrape(String orgId) {
        List<CatListing> results = new ArrayList<>();

        // scans all pet profiles through listing page
        List<String> animalUrls = scrapeListingPage(orgId);
        System.out.println("Found " + animalUrls.size() + " animals for org " + orgId);

        // goes to each pet profile and adds them in
        for (String animalUrl : animalUrls) {
            try {
                Thread.sleep(500); // so we dont spam their servers
                CatListing pet = scrapeProfile(animalUrl, orgId);
                if (pet != null) {
                    results.add(pet);
                    System.out.println("Scraped: " + pet.getName());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Total scraped for org " + orgId + ": " + results.size());
        return results;
    }

    /**
     * Scrapes the listing page for an org and returns all animal profile URLs.
     */
    private List<String> scrapeListingPage(String orgId) {
        List<String> urls = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(BASE_URL + "/embed/" + orgId)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // the grid of animal cards
            Element grid = doc.selectFirst("#iframe-animals-grid");
            if (grid == null) {
                System.err.println("Could not find animal grid for org " + orgId);
                return urls;
            }

            // each card is a div containing an <a> link to the detail page
            for (Element card : grid.select("div.px-2.my-4")) {
                Element link = card.selectFirst("a[href]");
                if (link != null) {
                    String href = link.absUrl("href");
                    if (!href.isEmpty()) {
                        urls.add(href);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to scrape listing page for org " + orgId + ": " + e.getMessage());
        }
        return urls;
    }

    private CatListing scrapeProfile(String animalUrl, String orgId) {
        try {
            Document doc = Jsoup.connect(animalUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            CatListing pet = new CatListing();
            pet.setPlatform("shelterluv");
            pet.setSourceUrl(animalUrl);
            pet.setSourceShelter(orgId);

            // takes in name at beginning of profile
            Element nameEl = doc.selectFirst("h1.text-2xl");
            pet.setName(nameEl != null ? nameEl.text() : "Unknown");

            // takes pics from profile
            Element photo = doc.selectFirst("img[alt^='Photo 1']");
            pet.setPhotoUrl(photo != null ? photo.attr("src") : "");

            // takes description of kitty
            Element desc = doc.selectFirst("div.mt-4.w-full.text-gray-600");
            pet.setDescription(desc != null ? desc.text() : "");

            // gets other kitty info like breed, age and size
            for (Element row : doc.select("div.flex.mb-6.items-end")) {
                Element labelEl = row.selectFirst("div.uppercase");
                Element valueEl = row.selectFirst("div.pl-2");

                if (labelEl == null || valueEl == null)
                    continue;

                String label = labelEl.text().trim().toLowerCase();
                String value = valueEl.text().trim();

                switch (label) {
                    case "breed" -> pet.setBreed(value);
                    case "sex" -> pet.setSex(value);
                    case "age" -> pet.setAge(value);
                    case "weight" -> pet.setSize(value);
                    case "color" -> pet.setColor(value);
                    case "adoption fee" -> pet.setAdoptionFee(value);
                }
            }

            return pet;

        } catch (IOException e) {
            System.err.println("Failed to scrape profile " + animalUrl + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getPlatformName() {
        return "shelterluv";
    }
}
