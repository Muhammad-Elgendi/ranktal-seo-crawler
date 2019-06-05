package backlinkscrawler.db.impl;

import backlinkscrawler.db.Backlink;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import backlinkscrawler.SampleLauncher;
import backlinkscrawler.db.PostgresDBService;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;


public class PostgresDBServiceImpl implements PostgresDBService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PostgresDBServiceImpl.class);

    private ComboPooledDataSource comboPooledDataSource;

    private PreparedStatement removeBacklinkStatement,insertBacklinkStatement;

    public PostgresDBServiceImpl(ComboPooledDataSource comboPooledDataSource) throws SQLException {
        this.comboPooledDataSource = comboPooledDataSource;
//        init();
    }

    private void init() throws SQLException {

        ///XXX should be done via DDL script
//        comboPooledDataSource.getConnection().createStatement().executeUpdate(
//                "CREATE SEQUENCE id_master_seq" +
//                        "  INCREMENT 1" +
//                        "  MINVALUE 1 " +
//                        "  MAXVALUE 9223372036854775807" +
//                        "  START 6 " +
//                        "  CACHE 1;")
//        ;
//        comboPooledDataSource.getConnection().createStatement().executeUpdate(
//                "CREATE TABLE webpage" +
//                        " ( " +
//                        "  id bigint NOT NULL," +
//                        "  html TEXT," +
//                        "  text TEXT," +
//                        "  url varchar(4096)," +
//                        "  seen timestamp without time zone NOT NULL," +
//                        "  primary key (id)" +
//                        ")");
    }


    @Override
    public void close() {
        if (comboPooledDataSource != null) {
            comboPooledDataSource.close();
        }
    }

    @Override
    public void storeBacklinks(ArrayList<Backlink> backlinks) {
        final int batchSize = 1000;
        int count = 0;
        try {
            insertBacklinkStatement = comboPooledDataSource.getConnection().prepareStatement("insert into backlinks values " +
                    "(?,?,?,?,?)");
            for (Backlink backlink : backlinks) {
                insertBacklinkStatement.setString(1, backlink.getSourceUrl());
                insertBacklinkStatement.setString(2, backlink.getTargetUrl());
                insertBacklinkStatement.setString(3, backlink.getAnchorText());
                insertBacklinkStatement.setBoolean(4, backlink.getDoFollow());
                insertBacklinkStatement.setTimestamp(5, SampleLauncher.getCurrentTimeStamp());
                insertBacklinkStatement.addBatch();

                if (++count % batchSize == 0) {
                    insertBacklinkStatement.executeBatch();
                }
            }
            insertBacklinkStatement.executeBatch(); // insert remaining records

        } catch (SQLException e) {
            logger.error("SQL Exception while storing backlink", e);
        } finally {
//            if (insertBacklinkStatement != null) {
//                try {
//                    insertBacklinkStatement.close();
//                } catch (SQLException e) {
//                    logger.error("SQL Exception while closing insertBacklinkStatement'{}'", e);
//                }
//            }
            try {
                if (insertBacklinkStatement.getConnection() != null)
                    insertBacklinkStatement.getConnection().close();
            } catch (SQLException e) {
                logger.error("SQL Exception while closing connection insertBacklinkStatement'{}'",e);
            }
        }
    }
}
