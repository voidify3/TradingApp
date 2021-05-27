package ServerSideTests;

import ServerSide.*;
//import org.junit.Test;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestJDBCDataSource {
    JDBCDataSource j;
    //TODO: prepared statements

    @BeforeAll @Test //beforeAll not beforeEach because update and delete and select tests need some data
                        // to exist, so it's simpler to let the insert tests be some of that data
    public void setupDataSource() {
        //instantiate, delete everything
        //maybe do some queries and assert all tables exist and are empty?
        //but mostly the thing being ensured by the @test is there's no exception thrown
    }
    @Test
    public void successInsert() {
        //assert returns 1
    }
    @Test
    public void failInsert() {
        //assert returns 0
    }
    @Test
    public void oneRowInsertUOD() {
        //(no duplicate) assert returns 1
    }
    @Test
    public void twoRowsInsertUOD() {
        //(yes duplicate) assert returns 2
    }
    @Test
    public void successUpdate() {
        //assert returns 1
    }
    @Test
    public void failUpdate() {
        //assert returns 0
    }
    @Test
    public void successDelete() {
        //assert returns 1
    }
    @Test
    public void failDelete() {
        //assert returns 0
    }
    @Test
    public void successSelect() {
        //assert returns expected result set
    }
    @Test
    public void failSelect() {
        //assert returns empty result set
    }
    @Test
    public void clearEverything() {
        //delete everything once more
        //assert returns the number of records that exist at this point
    }
    @AfterAll
    public void done() {
        j.close();
    }
}