# Backend-Anesthesia Architecture

## System overview
- Spring Boot 3.4 + Java 21 application providing REST + STOMP/WebSocket endpoints for anesthesia workflows.
- Persistence uses PostgreSQL with MyBatis (XML mappers) for CRUD and reporting queries.
- Runtime slices are organized by business capability: patient onboarding, pre-op anesthesia assessment (PAA), intra-op area capture, recovery, waveform telemetry, and summary reporting (ARS/RER).

## Domain slices (code map)
- **Patient onboarding**: `controller.PatientController`, `service.PatientService`, `mapper.PatientMapper`, `pojo.Patient`.
- **Surgery/treatment record**: `controller.SurgeryController`, `service.SurgeryService`, `mapper.SurgeryMapper`, `pojo.Surgery`.
- **Pre-op anesthesia assessment (PAA)**: `controller.PaaInformationController`, `service.PaaInformationService`, `mapper.PaaInformationMapper`, DTO `PaaAssessmentRequest`, entity `PaaInformation`.
- **Intra-op anesthesia area**: `controller.SurgeryAreaController`, `service.SurgeryAreaService`, `mapper.SurgeryAreaMapper`, DTOs `SurgeryAreaDTO` / `SurgeryAreaRecordDTO`, entities `DrugPushLog`, `SurgeryStep`.
- **Recovery area**: `controller.RecoveryController`, `service.RecoveryService`, `mapper.RecoveryMapper`, entity `Recovery`.
- **Recovery event record (RER)**: `controller.RERController`, `service.RERService`, `mapper.RERMapper`, DTO `RecoveryEventRecord`.
- **Anesthesia record summary (ARS)**: `controller.ARSController`, `service.ARSService`, `mapper.ARSMapper`, DTO `AnesthesiaRecordSummaryDTO`.
- **Waveform telemetry**: `controller.WaveformController`, `service.WaveformService`, `mapper.WaveformMapper`, DTOs `WaveformDTO/WaveformSeriesDTO`, entities `Waveform`, `WaveformParameter`.
- **Device data ingestion**: `controller.DataController` receives device payloads; STOMP consumer `stomp.StompDataConsumer` streams telemetry into DB; `service.DeviceBindingService` pulls binding info from the binding service.

## Persistence & configuration
- Datasource and MyBatis settings live in `src/main/resources/application.yml`.
- Mapper XML files reside in `src/main/resources/mapper/`, one per mapper interface; type aliases point to `com.medical.pojo`.
- PostgreSQL schemas expected by mappers: `patient`, `treatment_information`, `paa_information`, `surgery_step`, `drug_push_log`, `recovery_*` tables, `waveform`/`waveform_parameter`.

## External integrations
- **Binding service**: `DeviceBindingServiceImpl` queries `${device.binding.url}` to resolve device→treatment binding.
- **STOMP/WebSocket**: `StompDataConsumer` subscribes to `${stomp.topic}` at `${stomp.server.url}`; batches waveform and parameter data into PostgreSQL.
- **MQTT**: legacy MQTT publisher has been removed; placeholder `utils.MqttManager` remains for compatibility but performs no work.

## Current cleanup (done in this pass)
- Removed unused data subscribe flow and MQTT implementation; dropped the MQTT dependency.
- Trimmed unused service methods (patient findAll/update, data subscribe) to keep APIs minimal.
- Fixed `application.yml` indentation and MyBatis alias package for correct loading.

## Portability tips
- Move secrets out of `application.yml` for deployments (env vars or Spring config server).
- Database-dependent mappers expect PostgreSQL extensions (e.g., `time_bucket` in `WaveformMapper`); ensure the target DB supports them or adapt the SQL.
- For further modularization, consider grouping code by domain package (e.g., `patient`, `surgery`, `telemetry`) and adding unit tests per slice to guard mapper/service behavior.
