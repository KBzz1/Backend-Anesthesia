package com.medical.mapper;

import com.medical.pojo.PaaInformation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaaInformationMapper {

    int insert(PaaInformation paaInformation);

    PaaInformation getBySurgeryId(Long surgeryId);

}
