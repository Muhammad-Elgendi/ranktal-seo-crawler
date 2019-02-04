package seocrawler;


import com.github.s3curitybug.similarityuniformfuzzyhash.UniformFuzzyHash;
import com.github.s3curitybug.similarityuniformfuzzyhash.UniformFuzzyHashes;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seocrawler.crawler.PostgresCrawlerFactory;
import seocrawler.crawler.Similarities;
import seocrawler.db.PostgresDBService;
import seocrawler.db.impl.PostgresDBServiceImpl;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Map;

public class SampleLauncher {

    private static final Logger logger = LoggerFactory.getLogger(SampleLauncher.class);

    public static  String mainUrl;
    public static Integer userId;
    public static Integer siteId;
    public static String matchPattern;
    public static boolean exactMatch;
    public static void main(String[] args) throws Exception {

//        if (args.length != 6) {
//            logger.info("Needed parameters: ");
//            logger.info("\t Seed URL (start crawling with this URL)");
//            logger.info("\t maxPagesToFetch (number of pages to be fetched)");
//            logger.info("\t nuberOfCrawlers (number of crawlers)");
//            logger.info("\t user id (id of user that request the crawling)");
//            logger.info("\t site id (id of site that being crawled)");
//            logger.info("\t Exact match (Crawling Exact match url or the same host)");
//            return;
//        }

        // Handle arguments
//        URL url = new URL(args[0]);
//        mainUrl= args[0];
//        int maxPages = Integer.valueOf(args[1]);
//        int numberOfCrawlers = Integer.valueOf(args[2]);
//        userId = Integer.valueOf(args[3]);
//        siteId = Integer.valueOf(args[4]);
//        exactMatch = Boolean.valueOf(args[5]);





        URL url = new URL("http://7loll.net");
        mainUrl= url.toString();
        userId = 1;
        siteId = 1;
        int maxPages = 10000;
        int numberOfCrawlers = 4;
        exactMatch = false;


        matchPattern = exactMatch ? mainUrl : url.getHost();

        logger.info("Crawler Started : ");
        logger.info("\t Seed URL : "+mainUrl);
        logger.info("\t maxPagesToFetch : "+maxPages);
        logger.info("\t nuberOfCrawlers : "+numberOfCrawlers);
        logger.info("\t user id : "+userId);
        logger.info("\t site id : "+siteId);

        CrawlConfig config = new CrawlConfig();

        config.setPolitenessDelay(1000);

        config.setCrawlStorageFolder("/media/muhammad/disk/crawlerData/"+url.getHost());

        config.setMaxPagesToFetch(maxPages);

        config.setRespectNoFollow(false);

        config.setRespectNoIndex(false);

        config.setUserAgentString("SEO-spider (https://github.com/Muhammad-Elgendi/SEO-spider/)");

        /*
         * Instantiate the controller for this crawl.
         */
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();

        robotstxtConfig.setUserAgentName("SEO-spider");

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed(mainUrl);
        Dotenv dotenv = Dotenv.configure().directory("./").load();

        logger.info("Delete Old URLS ... ");
        /**
         * Delete all old urls
         */
        deleteAllUrls(dotenv.get("JDBC_URL"),dotenv.get("DB_USER_NAME"),dotenv.get("DB_PASSWORD"),"org.postgresql.Driver");

        logger.info("Starting Crawling Process ... ");

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */

        controller.start(new PostgresCrawlerFactory(dotenv.get("JDBC_URL"),dotenv.get("DB_USER_NAME"),dotenv.get("DB_PASSWORD")), numberOfCrawlers);


        logger.info("Crawling Process Has Finished ... ");


        logger.info("Check Duplicate Content and Store Similarities Process Has Started ... ");

        /**
         * Check Duplicate Content and Store Similarities
         */
        checkForDuplicateContent(dotenv.get("JDBC_URL"),dotenv.get("DB_USER_NAME"),dotenv.get("DB_PASSWORD"),"org.postgresql.Driver");


        logger.info("Inform backend Process Has Started ... ");

        /**
         * Inform backend
         */
        notifyBackend(dotenv.get("JDBC_URL"),dotenv.get("DB_USER_NAME"),dotenv.get("DB_PASSWORD"),"org.postgresql.Driver","Finished", getCurrentTimeStamp());


        logger.info("The End");
    }


    private static void checkForDuplicateContent(String dbUrl, String dbUser, String dbPw, String driver) throws Exception{
        PostgresDBService postgresDBService = new PostgresDBServiceImpl(dbUrl,dbUser,dbPw,driver);
        Map<String,String> hashesStrings = postgresDBService.getHashes(mainUrl);
        Map<String, UniformFuzzyHash>  map = UniformFuzzyHashes.computeHashesFromStrings(hashesStrings,61);
        Map similarities= UniformFuzzyHashes.computeAllHashesSimilarities(map);
        Similarities.saveAllHashesSimilarities(similarities,postgresDBService);
    }

    private static void notifyBackend(String dbUrl, String dbUser, String dbPw, String driver, String status, Timestamp finishTime) throws Exception{
        PostgresDBService postgresDBService = new PostgresDBServiceImpl(dbUrl,dbUser,dbPw,driver);
        postgresDBService.updateJob(status,finishTime,siteId);
    }


    private static void deleteAllUrls(String dbUrl, String dbUser, String dbPw, String driver) throws Exception{
        PostgresDBService postgresDBService = new PostgresDBServiceImpl(dbUrl,dbUser,dbPw,driver);
        postgresDBService.removeSite(matchPattern);
    }

    public static java.sql.Timestamp getCurrentTimeStamp() {

        return new java.sql.Timestamp(new java.util.Date().getTime());

    }


}
