package com.medical.pojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecoveryJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesChinesePupilEqualValues() throws Exception {
        Recovery recovery = objectMapper.readValue("""
            {
              "treatmentInformationId": 1,
              "pupilEqual": "等大"
            }
            """, Recovery.class);

        assertEquals(Boolean.TRUE, recovery.getPupilEqual());
    }

    @Test
    void parsesBooleanPupilEqualValues() throws Exception {
        Recovery recovery = objectMapper.readValue("""
            {
              "treatmentInformationId": 1,
              "pupilEqual": false
            }
            """, Recovery.class);

        assertEquals(Boolean.FALSE, recovery.getPupilEqual());
    }

    @Test
    void treatsBlankPupilEqualAsNull() throws Exception {
        Recovery recovery = objectMapper.readValue("""
            {
              "treatmentInformationId": 1,
              "pupilEqual": " "
            }
            """, Recovery.class);

        assertNull(recovery.getPupilEqual());
    }
}
