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
        System.out.println(1 / 0);
        System.out.println(Integer.parseInt("aaaa211112"));
        System.out.println(Integer.parseInt("aaaa1qsaa211123212"));

            }
}
