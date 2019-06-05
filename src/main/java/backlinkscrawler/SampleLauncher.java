package backlinkscrawler;


import com.github.s3curitybug.similarityuniformfuzzyhash.UniformFuzzyHash;
import com.github.s3curitybug.similarityuniformfuzzyhash.UniformFuzzyHashes;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import backlinkscrawler.crawler.PostgresCrawlerFactory;
import java.net.URL;

public class SampleLauncher {

    private static final Logger logger = LoggerFactory.getLogger(SampleLauncher.class);

//    public static  String mainUrl;
//    public static Integer userId;
//    public static Integer siteId;
//    public static String matchPattern;
//    public static boolean exactMatch;
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

//        URL url = new URL("https://dmoztools.net");
//        mainUrl= url.toString();
//        userId = 1;
//        siteId = 2;
//        int maxPages = 10000;
        int numberOfCrawlers = 40;
//        exactMatch = false;


//        matchPattern = exactMatch ? mainUrl : url.getHost();

        logger.info("Crawler Started : ");
//        logger.info("\t Seed URL : "+mainUrl);
//        logger.info("\t maxPagesToFetch : "+maxPages);
        logger.info("\t nuberOfCrawlers : "+numberOfCrawlers);
//        logger.info("\t user id : "+userId);
//        logger.info("\t site id : "+siteId);

        Dotenv dotenv = Dotenv.configure().directory("./").load();

        CrawlConfig config = new CrawlConfig();

        config.setRedisHost(dotenv.get("REDIS_HOST"));
        config.setRedisPort(Integer.valueOf(dotenv.get("REDIS_PORT")));

        config.setPolitenessDelay(100);

        /*
         * You can set the maximum crawl depth here. The default value is -1 for
         * unlimited depth
         */
        config.setMaxDepthOfCrawling(-1);

//        config.setCrawlStorageFolder("/media/muhammad/disk/crawlerData/"+url.getHost());

        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        config.setMaxPagesToFetch(-1);

        /**
         * Do you want crawler4j to crawl also binary data ?
         * example: the contents of pdf, or the metadata of images etc
         */
        config.setIncludeBinaryContentInCrawling(false);

        /*
         * Do you need to set a proxy? If so, you can use:
         * config.setProxyHost("proxyserver.example.com");
         * config.setProxyPort(8080);
         *
         * If your proxy also needs authentication:
         * config.setProxyUsername(username); config.getProxyPassword(password);
         */

        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);

//        config.setRespectNoFollow(false);

//        config.setRespectNoIndex(false);

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
        controller.addSeed("https://en.wikipedia.org/");



        ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
        comboPooledDataSource.setDriverClass("org.postgresql.Driver");
        comboPooledDataSource.setJdbcUrl(dotenv.get("JDBC_URL"));
        comboPooledDataSource.setUser(dotenv.get("DB_USER_NAME"));
        comboPooledDataSource.setPassword(dotenv.get("DB_PASSWORD"));
        comboPooledDataSource.setMaxPoolSize(numberOfCrawlers);
        comboPooledDataSource.setMinPoolSize(numberOfCrawlers);


        logger.info("Starting Crawling Process ... ");

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */

        controller.start(new PostgresCrawlerFactory(comboPooledDataSource), numberOfCrawlers);


        logger.info("Crawling Process Has Finished ... ");



        comboPooledDataSource.close();

        logger.info("Connection Pool Has Closed ... ");

        logger.info("The End");
    }


    public static java.sql.Timestamp getCurrentTimeStamp() {

        return new java.sql.Timestamp(new java.util.Date().getTime());

    }

}