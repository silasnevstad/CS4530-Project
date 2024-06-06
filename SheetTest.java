import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SheetTest {

    Sheet exampleSheet = new Sheet("sheet1");

    void testName() {
        assertEquals("sheet1", exampleSheet.name());
    }

}
