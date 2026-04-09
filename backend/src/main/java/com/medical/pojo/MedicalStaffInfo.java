package com.medical.pojo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalStaffInfo {
    private Long staffId;
    private Long departmentId;
    private String name;
    private String gender;
    private Date dateOfBirth;
    private String position;
    private String title;
    private String phone;
    private String email;
    private Date hireDate;
    private Boolean isActive;
    private String remark;
    private Date createdAt;
    private Date updatedAt;
}
