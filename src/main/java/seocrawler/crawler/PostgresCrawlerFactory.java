package seocrawler.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import seocrawler.db.impl.PostgresDBServiceImpl;

public class PostgresCrawlerFactory implements CrawlController.WebCrawlerFactory<PostgresWebCrawler> {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPw;

    public PostgresCrawlerFactory(String dbUrl, String dbUser, String dbPw) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPw = dbPw;
    }

    public PostgresWebCrawler newInstance() throws Exception {
        return new PostgresWebCrawler(new PostgresDBServiceImpl(dbUrl,dbUser,dbPw,"org.postgresql.Driver"));
    }

}
