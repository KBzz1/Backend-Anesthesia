package com.medical.pojo;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PaaInformation {

    private Long paaId;
    private Long surgeryId;

    private BigDecimal height;
    private BigDecimal weight;
    private String idCardNumber;

    private Boolean hisIsHypertension;
    private Boolean hisIsChronicBronchitis;
    private Boolean hisIsOsas;
    private Boolean hisIsDiabetes;
    private Boolean hisIsAsthma;
    private Boolean hisIsStroke;
    private Boolean hisIsCoronaryHeartDisease;
    private Boolean hisIsArrhythmia;
    private Boolean hisIsCopd;
    private Boolean hisIsPepticUlcer;
    private Boolean hisIsThyroidDysfunction;
    private Boolean hisIsHematemesisOrMelena;

    private String hisOthers;
    private Boolean isLongTermMedication;
    private String medicationNameAndUsage;
    private String smokeHis;
    private String drinkHis;

    private Boolean isAnesthesiaHis;
    private String anesthesiaTypeAndAdverseReactionHis;
    private String anesthesiaSpecialConditionsHis;

    private String peVitalSignsStatus;
    private String bloodpresure;
    private Short pePulseRate;
    private Short peSpo2;
    private Boolean peIsHypertension;
    private Boolean peIsHypotension;
    private Boolean peIsTachycardia;
    private Boolean peIsBradycardia;
    private Boolean peIsTachypnea;
    private Boolean peIsFever;
    private String peConsciousness;

    private Boolean peIsAuscultationNormal;
    private Boolean peIsBreathSoundCoarse;
    private Boolean peIsMoistRales;
    private Boolean peIsWheezing;
    private Boolean peIsBreathSoundAsymmetric;
    private Boolean peIsBreathSoundDiminished;
    private Boolean peIsHeartMurmur;
    private Boolean peIsArrhythmia;
    private Boolean peIsCardiopulmonaryAuscultationNormal;
    private Boolean peIsMouthOpenLt3cm;
    private Boolean peIsThyromentalDistanceLt6cm;
    private Boolean peIsMicrognathia;
    private Boolean peIsMallampatiGe3;
    private Boolean peIsDentitionAbnormal;
    private Boolean peIsLimitedNeckMobility;
    private Boolean peIsAirwayNormal;
    private Boolean peIsPunctureSiteNormal;
    private Boolean peIsPunctureSiteInfected;
    private Boolean peIsPunctureSiteUlcerated;
    private Boolean peIsSpinalMalformation;

    private String labBloodRoutineStatus;
    private String labEcgStatus;
    private String labChestXrayStatus;
    private String labBloodGasStatus;
    private String labDynamicEcgStatus;
    private String labCardiacUltrasoundStatus;
    private String labLungFunctionStatus;
    private String labCoagulationStatus;
    private String labElectrolyteStatus;
    private String labHepaticRenalFunction;
    private String labElse;
    private String labCoronaryAngiography;
    private String labAbnormalDescription;

    private String asaClass;
    private String cardiacFunctionClass;
    private String airwayDifficulty;
    private String anesthesiaRiskLevel;
    private String furtherDiagnosisPlan;

    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String chiefComplaint;
    private String specialRiskNotice;
    private String agentRelationship;
    private String asaClassSuggestion;

    public Long getPaaId() {
        return paaId;
    }

    public void setPaaId(Long paaId) {
        this.paaId = paaId;
    }

    public Long getSurgeryId() {
        return surgeryId;
    }

    public void setSurgeryId(Long surgeryId) {
        this.surgeryId = surgeryId;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public Boolean getHisIsHypertension() {
        return hisIsHypertension;
    }

    public void setHisIsHypertension(Boolean hisIsHypertension) {
        this.hisIsHypertension = hisIsHypertension;
    }

    public Boolean getHisIsChronicBronchitis() {
        return hisIsChronicBronchitis;
    }

    public void setHisIsChronicBronchitis(Boolean hisIsChronicBronchitis) {
        this.hisIsChronicBronchitis = hisIsChronicBronchitis;
    }

    public Boolean getHisIsOsas() {
        return hisIsOsas;
    }

    public void setHisIsOsas(Boolean hisIsOsas) {
        this.hisIsOsas = hisIsOsas;
    }

    public Boolean getHisIsDiabetes() {
        return hisIsDiabetes;
    }

    public void setHisIsDiabetes(Boolean hisIsDiabetes) {
        this.hisIsDiabetes = hisIsDiabetes;
    }

    public Boolean getHisIsAsthma() {
        return hisIsAsthma;
    }

    public void setHisIsAsthma(Boolean hisIsAsthma) {
        this.hisIsAsthma = hisIsAsthma;
    }

    public Boolean getHisIsStroke() {
        return hisIsStroke;
    }

    public void setHisIsStroke(Boolean hisIsStroke) {
        this.hisIsStroke = hisIsStroke;
    }

    public Boolean getHisIsCoronaryHeartDisease() {
        return hisIsCoronaryHeartDisease;
    }

    public void setHisIsCoronaryHeartDisease(Boolean hisIsCoronaryHeartDisease) {
        this.hisIsCoronaryHeartDisease = hisIsCoronaryHeartDisease;
    }

    public Boolean getHisIsArrhythmia() {
        return hisIsArrhythmia;
    }

    public void setHisIsArrhythmia(Boolean hisIsArrhythmia) {
        this.hisIsArrhythmia = hisIsArrhythmia;
    }

    public Boolean getHisIsCopd() {
        return hisIsCopd;
    }

    public void setHisIsCopd(Boolean hisIsCopd) {
        this.hisIsCopd = hisIsCopd;
    }

    public Boolean getHisIsPepticUlcer() {
        return hisIsPepticUlcer;
    }

    public void setHisIsPepticUlcer(Boolean hisIsPepticUlcer) {
        this.hisIsPepticUlcer = hisIsPepticUlcer;
    }

    public Boolean getHisIsThyroidDysfunction() {
        return hisIsThyroidDysfunction;
    }

    public void setHisIsThyroidDysfunction(Boolean hisIsThyroidDysfunction) {
        this.hisIsThyroidDysfunction = hisIsThyroidDysfunction;
    }

    public Boolean getHisIsHematemesisOrMelena() {
        return hisIsHematemesisOrMelena;
    }

    public void setHisIsHematemesisOrMelena(Boolean hisIsHematemesisOrMelena) {
        this.hisIsHematemesisOrMelena = hisIsHematemesisOrMelena;
    }

    public String getHisOthers() {
        return hisOthers;
    }

    public void setHisOthers(String hisOthers) {
        this.hisOthers = hisOthers;
    }

    public Boolean getLongTermMedication() {
        return isLongTermMedication;
    }

    public void setLongTermMedication(Boolean longTermMedication) {
        isLongTermMedication = longTermMedication;
    }

    public String getMedicationNameAndUsage() {
        return medicationNameAndUsage;
    }

    public void setMedicationNameAndUsage(String medicationNameAndUsage) {
        this.medicationNameAndUsage = medicationNameAndUsage;
    }

    public String getSmokeHis() {
        return smokeHis;
    }

    public void setSmokeHis(String smokeHis) {
        this.smokeHis = smokeHis;
    }

    public String getDrinkHis() {
        return drinkHis;
    }

    public void setDrinkHis(String drinkHis) {
        this.drinkHis = drinkHis;
    }

    public Boolean getAnesthesiaHis() {
        return isAnesthesiaHis;
    }

    public void setAnesthesiaHis(Boolean anesthesiaHis) {
        isAnesthesiaHis = anesthesiaHis;
    }

    public String getAnesthesiaTypeAndAdverseReactionHis() {
        return anesthesiaTypeAndAdverseReactionHis;
    }

    public void setAnesthesiaTypeAndAdverseReactionHis(String anesthesiaTypeAndAdverseReactionHis) {
        this.anesthesiaTypeAndAdverseReactionHis = anesthesiaTypeAndAdverseReactionHis;
    }

    public String getAnesthesiaSpecialConditionsHis() {
        return anesthesiaSpecialConditionsHis;
    }

    public void setAnesthesiaSpecialConditionsHis(String anesthesiaSpecialConditionsHis) {
        this.anesthesiaSpecialConditionsHis = anesthesiaSpecialConditionsHis;
    }

    public String getPeVitalSignsStatus() {
        return peVitalSignsStatus;
    }

    public void setPeVitalSignsStatus(String peVitalSignsStatus) {
        this.peVitalSignsStatus = peVitalSignsStatus;
    }

    public Boolean getPeIsAirwayNormal() {
        return peIsAirwayNormal;
    }

    public void setPeIsAirwayNormal(Boolean peIsAirwayNormal) {
        this.peIsAirwayNormal = peIsAirwayNormal;
    }

    public String getBloodpresure() {
        return bloodpresure;
    }

    public void setBloodpresure(String bloodpresure) {
        this.bloodpresure = bloodpresure;
    }

    public Short getPePulseRate() {
        return pePulseRate;
    }

    public void setPePulseRate(Short pePulseRate) {
        this.pePulseRate = pePulseRate;
    }

    public Short getPeSpo2() {
        return peSpo2;
    }

    public void setPeSpo2(Short peSpo2) {
        this.peSpo2 = peSpo2;
    }

    public Boolean getPeIsHypertension() {
        return peIsHypertension;
    }

    public void setPeIsHypertension(Boolean peIsHypertension) {
        this.peIsHypertension = peIsHypertension;
    }

    public Boolean getPeIsHypotension() {
        return peIsHypotension;
    }

    public void setPeIsHypotension(Boolean peIsHypotension) {
        this.peIsHypotension = peIsHypotension;
    }

    public Boolean getPeIsTachycardia() {
        return peIsTachycardia;
    }

    public void setPeIsTachycardia(Boolean peIsTachycardia) {
        this.peIsTachycardia = peIsTachycardia;
    }

    public Boolean getPeIsBradycardia() {
        return peIsBradycardia;
    }

    public void setPeIsBradycardia(Boolean peIsBradycardia) {
        this.peIsBradycardia = peIsBradycardia;
    }

    public Boolean getPeIsTachypnea() {
        return peIsTachypnea;
    }

    public void setPeIsTachypnea(Boolean peIsTachypnea) {
        this.peIsTachypnea = peIsTachypnea;
    }

    public Boolean getPeIsFever() {
        return peIsFever;
    }

    public void setPeIsFever(Boolean peIsFever) {
        this.peIsFever = peIsFever;
    }

    public String getPeConsciousness() {
        return peConsciousness;
    }

    public void setPeConsciousness(String peConsciousness) {
        this.peConsciousness = peConsciousness;
    }

    public Boolean getPeIsAuscultationNormal() {
        return peIsAuscultationNormal;
    }

    public void setPeIsAuscultationNormal(Boolean peIsAuscultationNormal) {
        this.peIsAuscultationNormal = peIsAuscultationNormal;
    }

    public Boolean getPeIsBreathSoundCoarse() {
        return peIsBreathSoundCoarse;
    }

    public void setPeIsBreathSoundCoarse(Boolean peIsBreathSoundCoarse) {
        this.peIsBreathSoundCoarse = peIsBreathSoundCoarse;
    }

    public Boolean getPeIsMoistRales() {
        return peIsMoistRales;
    }

    public void setPeIsMoistRales(Boolean peIsMoistRales) {
        this.peIsMoistRales = peIsMoistRales;
    }

    public Boolean getPeIsWheezing() {
        return peIsWheezing;
    }

    public void setPeIsWheezing(Boolean peIsWheezing) {
        this.peIsWheezing = peIsWheezing;
    }

    public Boolean getPeIsBreathSoundAsymmetric() {
        return peIsBreathSoundAsymmetric;
    }

    public void setPeIsBreathSoundAsymmetric(Boolean peIsBreathSoundAsymmetric) {
        this.peIsBreathSoundAsymmetric = peIsBreathSoundAsymmetric;
    }

    public Boolean getPeIsBreathSoundDiminished() {
        return peIsBreathSoundDiminished;
    }

    public void setPeIsBreathSoundDiminished(Boolean peIsBreathSoundDiminished) {
        this.peIsBreathSoundDiminished = peIsBreathSoundDiminished;
    }

    public Boolean getPeIsHeartMurmur() {
        return peIsHeartMurmur;
    }

    public void setPeIsHeartMurmur(Boolean peIsHeartMurmur) {
        this.peIsHeartMurmur = peIsHeartMurmur;
    }

    public Boolean getPeIsArrhythmia() {
        return peIsArrhythmia;
    }

    public void setPeIsArrhythmia(Boolean peIsArrhythmia) {
        this.peIsArrhythmia = peIsArrhythmia;
    }

    public Boolean getPeIsCardiopulmonaryAuscultationNormal() {
        return peIsCardiopulmonaryAuscultationNormal;
    }

    public void setPeIsCardiopulmonaryAuscultationNormal(Boolean peIsCardiopulmonaryAuscultationNormal) {
        this.peIsCardiopulmonaryAuscultationNormal = peIsCardiopulmonaryAuscultationNormal;
    }

    public Boolean getPeIsMouthOpenLt3cm() {
        return peIsMouthOpenLt3cm;
    }

    public void setPeIsMouthOpenLt3cm(Boolean peIsMouthOpenLt3cm) {
        this.peIsMouthOpenLt3cm = peIsMouthOpenLt3cm;
    }

    public Boolean getPeIsThyromentalDistanceLt6cm() {
        return peIsThyromentalDistanceLt6cm;
    }

    public void setPeIsThyromentalDistanceLt6cm(Boolean peIsThyromentalDistanceLt6cm) {
        this.peIsThyromentalDistanceLt6cm = peIsThyromentalDistanceLt6cm;
    }

    public Boolean getPeIsMicrognathia() {
        return peIsMicrognathia;
    }

    public void setPeIsMicrognathia(Boolean peIsMicrognathia) {
        this.peIsMicrognathia = peIsMicrognathia;
    }

    public Boolean getPeIsMallampatiGe3() {
        return peIsMallampatiGe3;
    }

    public void setPeIsMallampatiGe3(Boolean peIsMallampatiGe3) {
        this.peIsMallampatiGe3 = peIsMallampatiGe3;
    }

    public Boolean getPeIsDentitionAbnormal() {
        return peIsDentitionAbnormal;
    }

    public void setPeIsDentitionAbnormal(Boolean peIsDentitionAbnormal) {
        this.peIsDentitionAbnormal = peIsDentitionAbnormal;
    }

    public Boolean getPeIsLimitedNeckMobility() {
        return peIsLimitedNeckMobility;
    }

    public void setPeIsLimitedNeckMobility(Boolean peIsLimitedNeckMobility) {
        this.peIsLimitedNeckMobility = peIsLimitedNeckMobility;
    }

    public Boolean getPeIsPunctureSiteNormal() {
        return peIsPunctureSiteNormal;
    }

    public void setPeIsPunctureSiteNormal(Boolean peIsPunctureSiteNormal) {
        this.peIsPunctureSiteNormal = peIsPunctureSiteNormal;
    }

    public Boolean getPeIsPunctureSiteInfected() {
        return peIsPunctureSiteInfected;
    }

    public void setPeIsPunctureSiteInfected(Boolean peIsPunctureSiteInfected) {
        this.peIsPunctureSiteInfected = peIsPunctureSiteInfected;
    }

    public Boolean getPeIsPunctureSiteUlcerated() {
        return peIsPunctureSiteUlcerated;
    }

    public void setPeIsPunctureSiteUlcerated(Boolean peIsPunctureSiteUlcerated) {
        this.peIsPunctureSiteUlcerated = peIsPunctureSiteUlcerated;
    }

    public Boolean getPeIsSpinalMalformation() {
        return peIsSpinalMalformation;
    }

    public void setPeIsSpinalMalformation(Boolean peIsSpinalMalformation) {
        this.peIsSpinalMalformation = peIsSpinalMalformation;
    }

    public String getLabBloodRoutineStatus() {
        return labBloodRoutineStatus;
    }

    public void setLabBloodRoutineStatus(String labBloodRoutineStatus) {
        this.labBloodRoutineStatus = labBloodRoutineStatus;
    }

    public String getLabEcgStatus() {
        return labEcgStatus;
    }

    public void setLabEcgStatus(String labEcgStatus) {
        this.labEcgStatus = labEcgStatus;
    }

    public String getLabChestXrayStatus() {
        return labChestXrayStatus;
    }

    public void setLabChestXrayStatus(String labChestXrayStatus) {
        this.labChestXrayStatus = labChestXrayStatus;
    }

    public String getLabBloodGasStatus() {
        return labBloodGasStatus;
    }

    public void setLabBloodGasStatus(String labBloodGasStatus) {
        this.labBloodGasStatus = labBloodGasStatus;
    }

    public String getLabDynamicEcgStatus() {
        return labDynamicEcgStatus;
    }

    public void setLabDynamicEcgStatus(String labDynamicEcgStatus) {
        this.labDynamicEcgStatus = labDynamicEcgStatus;
    }

    public String getLabCardiacUltrasoundStatus() {
        return labCardiacUltrasoundStatus;
    }

    public void setLabCardiacUltrasoundStatus(String labCardiacUltrasoundStatus) {
        this.labCardiacUltrasoundStatus = labCardiacUltrasoundStatus;
    }

    public String getLabLungFunctionStatus() {
        return labLungFunctionStatus;
    }

    public void setLabLungFunctionStatus(String labLungFunctionStatus) {
        this.labLungFunctionStatus = labLungFunctionStatus;
    }

    public String getLabCoagulationStatus() {
        return labCoagulationStatus;
    }

    public void setLabCoagulationStatus(String labCoagulationStatus) {
        this.labCoagulationStatus = labCoagulationStatus;
    }

    public String getLabElectrolyteStatus() {
        return labElectrolyteStatus;
    }

    public void setLabElectrolyteStatus(String labElectrolyteStatus) {
        this.labElectrolyteStatus = labElectrolyteStatus;
    }

    public String getLabHepaticRenalFunction() {
        return labHepaticRenalFunction;
    }

    public void setLabHepaticRenalFunction(String labHepaticRenalFunction) {
        this.labHepaticRenalFunction = labHepaticRenalFunction;
    }

    public String getLabElse() {
        return labElse;
    }

    public void setLabElse(String labElse) {
        this.labElse = labElse;
    }

    public String getLabAbnormalDescription() {
        return labAbnormalDescription;
    }

    public void setLabAbnormalDescription(String labAbnormalDescription) {
        this.labAbnormalDescription = labAbnormalDescription;
    }

    public String getLabCoronaryAngiography() {
        return labCoronaryAngiography;
    }

    public void setLabCoronaryAngiography(String labCoronaryAngiography) {
        this.labCoronaryAngiography = labCoronaryAngiography;
    }

    public String getAsaClass() {
        return asaClass;
    }

    public void setAsaClass(String asaClass) {
        this.asaClass = asaClass;
    }

    public String getCardiacFunctionClass() {
        return cardiacFunctionClass;
    }

    public void setCardiacFunctionClass(String cardiacFunctionClass) {
        this.cardiacFunctionClass = cardiacFunctionClass;
    }

    public String getAirwayDifficulty() {
        return airwayDifficulty;
    }

    public void setAirwayDifficulty(String airwayDifficulty) {
        this.airwayDifficulty = airwayDifficulty;
    }

    public String getAnesthesiaRiskLevel() {
        return anesthesiaRiskLevel;
    }

    public void setAnesthesiaRiskLevel(String anesthesiaRiskLevel) {
        this.anesthesiaRiskLevel = anesthesiaRiskLevel;
    }

    public String getFurtherDiagnosisPlan() {
        return furtherDiagnosisPlan;
    }

    public void setFurtherDiagnosisPlan(String furtherDiagnosisPlan) {
        this.furtherDiagnosisPlan = furtherDiagnosisPlan;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }

    public String getSpecialRiskNotice() {
        return specialRiskNotice;
    }

    public void setSpecialRiskNotice(String specialRiskNotice) {
        this.specialRiskNotice = specialRiskNotice;
    }

    public String getAgentRelationship() {
        return agentRelationship;
    }

    public void setAgentRelationship(String agentRelationship) {
        this.agentRelationship = agentRelationship;
    }

    public String getAsaClassSuggestion() {
        return asaClassSuggestion;
    }

    public void setAsaClassSuggestion(String asaClassSuggestion) {
        this.asaClassSuggestion = asaClassSuggestion;
    }
}
