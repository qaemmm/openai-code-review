package plus.gaga.middleware.test;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Test
    public void test() {
        System.out.println(Integer.parseInt("aaaa22"));
        System.out.println(Integer.parseInt("aaaa23"));
        System.out.println(Integer.parseInt("aaaa25"));
    }

}
