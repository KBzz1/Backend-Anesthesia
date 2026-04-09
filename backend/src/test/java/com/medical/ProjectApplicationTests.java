package com.medical;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectApplicationTests {

    @Test
    void mainMethodSignatureIsStable() throws Exception {
        Method mainMethod = ProjectApplication.class.getDeclaredMethod("main", String[].class);
        assertTrue(Modifier.isStatic(mainMethod.getModifiers()));
        assertEquals(void.class, mainMethod.getReturnType());
    }

}
