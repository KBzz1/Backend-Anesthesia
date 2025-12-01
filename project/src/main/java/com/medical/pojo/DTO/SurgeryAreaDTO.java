package com.medical.pojo.DTO;

import com.medical.pojo.PaaInformation;
import com.medical.pojo.Patient;
import com.medical.pojo.Surgery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurgeryAreaDTO {
    private Patient patient;
    private Surgery treatmentInformation;
    private PaaInformation paa;
}
