package com.medical.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Recovery {

	private Integer recoveryRoomRecordId;
	private Integer treatmentInformationId;

	private Integer bp;
    @JsonProperty("pBpm")
	private Integer pBpm;
    @JsonProperty("rBpm")
	private Integer rBpm;
	private Integer spo2;
	private String anesthesiaSatisfaction;
	private Integer vasScore;
	private Boolean recoveryConscious;
	private String skinCondition;
	private Integer stewardScore;
	private String awakeningLevel;
	private String airwayPatency;
	private String limbActivity;
	private String pupilLightReflex;
	private Integer respirationVt;
	private Integer muscleStrength;
	private Integer topRatio;
	private String respirationSound;
	private String reflex;
	private String sound;
	private String selfReportAbility;
	private Boolean consciousnessOrientation;
	private Boolean spatialOrientation;
	private Boolean calculationAbility;
	private Boolean memory;
	private String pupilEqual;
}



