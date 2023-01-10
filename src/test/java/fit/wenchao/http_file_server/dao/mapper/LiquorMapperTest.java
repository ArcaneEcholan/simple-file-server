package fit.wenchao.http_file_server.dao.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LiquorMapperTest {

    @Autowired
    LiquorMapper liquorMapper;

    @Test
    public void test() {
        System.out.println(liquorMapper.selectList(null));
    }
}