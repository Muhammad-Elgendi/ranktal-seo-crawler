package backlinkscrawler.db;

import java.util.ArrayList;


public interface PostgresDBService {
    void close();
    void storeBacklinks(ArrayList<Backlink> backlinks);
}
