package backlinkscrawler.crawler;

import backlinkscrawler.db.Backlink;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import backlinkscrawler.SampleLauncher;
import backlinkscrawler.db.PostgresDBService;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class PostgresWebCrawler extends WebCrawler {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PostgresWebCrawler.class);

    private static Pattern FILE_ENDING_EXCLUSION_PATTERN = Pattern.compile(".*(\\.(" +
            "css|js" +
            "|bmp|gif|jpe?g|JPE?G|png|tiff?|ico|nef|raw" +
            "|mid|mp2|mp3|mp4|wav|wma|flv|mpe?g" +
            "|avi|mov|mpeg|ram|m4v|wmv|rm|smil" +
            "|pdf|doc|docx|pub|xls|xlsx|vsd|ppt|pptx" +
            "|swf" +
            "|zip|rar|gz|bz2|7z|bin" +
            "|xml|txt|java|c|cpp|exe" +
            "))$");


    private final PostgresDBService postgresDBService;

    private ArrayList<Backlink> buffer;

    public PostgresWebCrawler(PostgresDBService postgresDBService) {
        this.postgresDBService = postgresDBService;
        buffer = new ArrayList();
    }

    /**
     * This method receives two parameters. The first parameter is the page in
     * which we have discovered this new url and the second parameter is the new
     * url. You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic). In this example,
     * we are instructing the crawler to ignore urls that have css, js, git, ...
     * extensions and to only accept urls that start with
     * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
     * parameter to make the decision.
     *
     * @param referringPage
     * @param url
     * @return
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {

        return !FILE_ENDING_EXCLUSION_PATTERN.matcher(url.getURL().toLowerCase()).matches();
    }

    @Override
    public void visit(Page page) {
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Document doc = Jsoup.parse(htmlParseData.getHtml());

            // decode url
            String url;
            try {
                url = URLDecoder.decode(page.getWebURL().getURL(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                url = page.getWebURL().getURL();
                logger.error("Decoding url in visit() failed", e);
            }

            // remove trailing slash
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            Header[] headers = page.getFetchResponseHeaders();
            boolean isFollow = true;
            for (Header header : headers) {
                if (header.getName().equals("X-Robots-Tag")) {
                    if (header.getValue().contains("nofollow")) {
                        isFollow = false;
                    }
                    if (header.getValue().contains("none")) {
                        isFollow = false;
                    }
                }
            }
            Elements robotsTags = doc.selectFirst("head").select("meta[name=robots]");
            if (!robotsTags.isEmpty()) {
                for (Element tag : robotsTags) {
                    if (tag.attr("content").contains("nofollow")) {
                        isFollow = false;
                    }
                }
            }
            // Get Outbound links
            Elements links = doc.select("a[href]");
            // is this link external
            boolean isExternal;

            for (Element link : links) {
                isExternal = !link.attr("abs:href").isEmpty() && !link.attr("abs:href").contains(page.getWebURL().getDomain());

                if (isExternal) {
                    // decode url
                    String backlink;
                    try {
                        backlink = URLDecoder.decode(link.attr("abs:href").toLowerCase(), "UTF-8");
                        if (backlink.endsWith("/")) {
                            backlink = backlink.substring(0, backlink.length() - 1);
                        }
                    } catch (UnsupportedEncodingException e) {
                        backlink = link.attr("abs:href").toLowerCase();
                        if (backlink.endsWith("/")) {
                            backlink = backlink.substring(0, backlink.length() - 1);
                        }
                        logger.error("Decoding backlink in visit() failed", e);
                    }

                    isFollow = isFollow ? !link.attr("rel").contains("nofollow") : isFollow;

                    // store new backlinks
                    buffer.add(new Backlink(url, backlink, link.html(), isFollow));
                }
            }

        }

        // persisting in-memory data
        if (buffer.size() >= 900){
            logger.info("--- Persisting in-memory data: "+ buffer.size());
            try {
                postgresDBService.storeBacklinks(buffer);
                buffer = new ArrayList<Backlink>();
            } catch (RuntimeException e) {
                logger.error("Storing backlinks failed", e);
            }
        }
    }

    /**
     * This function is called just before the termination of the current
     * crawler instance. It can be used for persisting in-memory data or other
     * finalization tasks.
     */
//    public void onBeforeExit() {
//        if (postgresDBService != null) {
//            postgresDBService.close();
//        }
//    }




}
