package com.medical.pojo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import lombok.AccessLevel;

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
    @Setter(AccessLevel.NONE)
	private Boolean pupilEqual;

    @JsonSetter("pupilEqual")
    public void setPupilEqual(Object pupilEqual) {
        this.pupilEqual = parsePupilEqual(pupilEqual);
    }

    private Boolean parsePupilEqual(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }

        String normalized = value.toString().trim();
        if (normalized.isEmpty()) {
            return null;
        }

        return switch (normalized.toLowerCase()) {
            case "true", "1", "yes", "y", "等大", "是" -> true;
            case "false", "0", "no", "n", "不等大", "否" -> false;
            default -> throw new IllegalArgumentException("Unsupported pupilEqual value: " + value);
        };
    }
}



